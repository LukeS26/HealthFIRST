package LukeS26.github.io;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.FindIterable;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jetty.http.HttpStatus;
import org.mindrot.jbcrypt.BCrypt;

import LukeS26.github.io.dataschema.Comment;
import LukeS26.github.io.dataschema.Post;
import LukeS26.github.io.dataschema.Account;
import LukeS26.github.io.dataschema.Token;
import io.javalin.Javalin;

public class HttpServer {
    // TODO: There is ZERO error handling. Everything is assumed to be sent
    // correctly and for the requested information to be available in the database.
    // You should handle errors
    public MongoManager mongoManager;
    private Javalin app;

    public HttpServer() {
        System.out.println("Initializing MongoDB....");
        mongoManager = new MongoManager();
        System.out.println("Finished initializing MongoDB.");

        System.out.println("Initializing Javalin...");
        app = Javalin.create().start(Settings.HTTP_SERVER_PORT);
        System.out.println("Finished initializing Javalin.");
    }

    public void start() {
        // insertTestAccount(); // Used for testing

        // #region Authentication
        app.post("/api/account/login", ctx -> {
            System.out.println("POST request for login from " + ctx.ip());

            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!doc.containsKey("username") || !doc.containsKey("password") || !doc.containsKey("expire")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Account loginAccount = mongoManager.getAccount((String) doc.get("username"));
            System.out.println("Found account");

            if (BCrypt.checkpw((String) doc.get("password"), loginAccount.passwordHash)) {
                System.out.println("Correct password");
                Token token = new Token((String) doc.get("username"), (boolean) doc.get("expire"));
                System.out.println("Writing token");
                mongoManager.writeToken(token);
    
                String tokenJson = token.toDoc().toJson();
    
                ctx.result(tokenJson);
                ctx.status(HttpStatus.CREATED_201);

            } else {
                ctx.status(HttpStatus.FORBIDDEN_403);
            }

        });
        // #endregion

        // #region Replies
        app.get("/api/replies/*", ctx -> {
            System.out.println("GET request for reply " + ctx.splat(0) + " from " + ctx.ip());

            FindIterable<Document> commentList = mongoManager.getReplies(ctx.splat(0));
            if (commentList == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                return;
            }

            Document replyDoc = new Document("replies", commentList);
            ctx.result(replyDoc.toJson());
            ctx.status(HttpStatus.OK_200);
        });
        // #endregion

        // #region Comments
        app.post("/api/comments", ctx -> {
            System.out.println("POST request to comment from " + ctx.ip());

            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!ctx.headerMap().containsKey("Authorization") ||!doc.containsKey("parent_id") || !doc.containsKey("body")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Token token = mongoManager.findToken(ctx.header("Authorization"));
            if (token != null) {
                Comment comment = new Comment();
                // only accept ObjectId objects instead of strings to stay consistent, because I
                // am sending it through GETs in the same format
                comment.parentId = new ObjectId(doc.get("parent_id").toString());
                comment.author = token.username;
                comment.body = (String) doc.get("body");
    
                mongoManager.writeComment(comment);
                ctx.status(HttpStatus.CREATED_201);

            } else {
                ctx.status(HttpStatus.FORBIDDEN_403);
            }

        });

        app.get("/api/comments/*", ctx -> {
            System.out.println("GET request for comment " + ctx.splat(0) + " from " + ctx.ip());

            Comment comment = mongoManager.getComment(ctx.splat(0));
            if (comment != null) {
                String commentJson = comment.toDoc().toJson();

                ctx.result(commentJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                ctx.status(HttpStatus.NOT_FOUND_404);
            }
        });
        // #endregion

        // #region Posts
        app.post("/api/posts", ctx -> {
            System.out.println("POST request to post from " + ctx.ip());
            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!doc.containsKey("author") || !doc.containsKey("title") || !doc.containsKey("body")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Post post = new Post(); // Can't use Post.fromDoc because it doesn't contain an ID here
            /*
             * TODO: As of right now, anyone can make a post as anyone else by just editing
             * the request body. To fix this, check that the person submitting the request
             * is actually the author Instead of doing post.author = (String)
             * doc.get("author"), you should find the author's username by the authorization
             * token used in the request or whatever form of authorization we use
             */
            post.author = (String) doc.get("author");
            post.title = (String) doc.get("title");
            post.body = (String) doc.get("body");

            mongoManager.writePost(post);

            ctx.status(HttpStatus.CREATED_201);
        });

        app.get("/api/posts/*", ctx -> {
            System.out.println("GET request for post " + ctx.splat(0) + " from " + ctx.ip());

            Post post = mongoManager.getPost(ctx.splat(0));
            if (post != null) {
                String postJson = post.toDoc().toJson();

                ctx.result(postJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                // You could provide an error body here
                ctx.status(HttpStatus.NOT_FOUND_404);
            }
        });
        // #endregion

        // #region Accounts

        // User sends hashed password, salt is added to the end and then rehashed in
        // backend
        app.post("/api/account/signup", ctx -> {
            System.out.println("POST request to sign up from " + ctx.ip());
            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!doc.containsKey("username") || !doc.containsKey("first_name") || !doc.containsKey("last_name")
                    || !doc.containsKey("email") || !doc.containsKey("password_hash")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Account userAccount = new Account();
            /*
             * Can't use userAccount.fromDoc here because this won't contain a bio, userAccount
             * link, permission id, etc.
             */
            userAccount.username = (String) doc.get("username");
            userAccount.firstName = (String) doc.get("first_name");
            userAccount.lastName = (String) doc.get("last_name");
            userAccount.email = (String) doc.get("email");
            userAccount.bio = null;
            userAccount.profilePictureLink = null;

            String receivedHash = (String) doc.get("password_hash");

            String salt = BCrypt.gensalt(Settings.BCRYPT_LOG_ROUNDS);
            String finalPasswordHash = BCrypt.hashpw(receivedHash, salt);

            userAccount.passwordHash = finalPasswordHash;
            userAccount.permissionID = 0;
            userAccount.badgeIDs = new ArrayList<Integer>();

            mongoManager.writeAccount(userAccount);

            ctx.status(HttpStatus.CREATED_201);
        });

        app.get("/api/account/*", ctx -> {
            System.out.println("GET request for account " + ctx.splat(0) + " from " + ctx.ip());
            Account userAccount = mongoManager.getAccount(ctx.splat(0));
            if (userAccount != null) {
                Document userAccountDoc = userAccount.toDoc(false);
                String accountJson = userAccountDoc.toJson();

                ctx.result(accountJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                // You could provide an error body here
                ctx.status(HttpStatus.NOT_FOUND_404);
            }
        });
        // #endregion
    }

    public boolean verifyToken(String token) {
        
        return false;
    }

    public void testLogin(String username, String password) {
        Account account = mongoManager.getAccount(username);
        if (BCrypt.checkpw(password, account.passwordHash)) {
            System.out.println("Password is a match");

        } else {
            System.out.println("Password is not a match");
        }
    }

    /**
     * Generates and adds a acount to the database containing test information
     */
    private void insertTestAccount() {
        List<Integer> badgeIDs = new ArrayList<>();
        badgeIDs.add(1);
        badgeIDs.add(5);
        badgeIDs.add(7);

        Account a = new Account();
        a.username = "JohnSmith72";
        a.passwordHash = "hash12345";
        a.firstName = "John";
        a.lastName = "Smith";
        a.email = "johnsmith@gmail.com";
        a.profilePictureLink = "https://LINK_TO_IMAGE/IMAGE_NAME.png";
        a.bio = "Example biography, this can be an empty string";
        a.permissionID = Utils.Permissions.USER.ordinal(); // Ordinal is index in enum
        a.badgeIDs = badgeIDs;

        mongoManager.writeAccount(a);
    }
}

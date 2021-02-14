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
    public MongoManager mongoManager;
    private Javalin app;

    public HttpServer() {
        System.out.println("Initializing MongoDB....");
        mongoManager = new MongoManager();
        System.out.println("Finished initializing MongoDB.");

        System.out.println("Initializing Javalin...");
        app = Javalin.create(config -> {
            config.requestLogger((ctx, ms) -> {
                System.out.println("[LOG] " + ctx.method() + " request to " + ctx.fullUrl() + " from userAgent: " + ctx.userAgent() + " and IP: " + ctx.ip());
            });

        }).start(Settings.HTTP_SERVER_PORT);
        System.out.println("Finished initializing Javalin.");
    }

    public void start() {
        // insertTestAccount(); // Used for testing

        // #region Authentication
        /**
         * Getting an account token + verifying username+pass
         */
        app.post("/api/account/login", ctx -> {
            if (ctx.headerMap().containsKey("Origin") && ctx.header("Origin").contains(Settings.WEBSITE_URL)) {
                // TODO: Probably a bad idea to accept requests from any origin
                ctx.res.setHeader("Access-Control-Allow-Origin", "*");
            }
            
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
        /**
         * Get all comments and comments on comments for a post
         */
        app.get("/api/replies/*", ctx -> {
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
        /**
         * Create a comment for the specified parent
         * Authorization completed
         */
        app.post("/api/comments", ctx -> {
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

        /**
         * Get a specific comment
         */
        app.get("/api/comments/*", ctx -> {
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
        /**
         * Create a post
         */
        app.post("/api/posts", ctx -> {
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
            post.author = (String) doc.get("author");
            post.title = (String) doc.get("title");
            post.body = (String) doc.get("body");

            mongoManager.writePost(post);

            ctx.status(HttpStatus.CREATED_201);
        });

        /**
         * Get a post
         */
        app.get("/api/posts/*", ctx -> {
            if (ctx.headerMap().containsKey("Origin") && ctx.header("Origin").contains(Settings.WEBSITE_URL)) {
                // TODO: Probably a bad idea to accept requests from any origin
                ctx.res.setHeader("Access-Control-Allow-Origin", "*");
            }

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

        /**
         * Create a new account
         */
        app.post("/api/account/signup", ctx -> {
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

        /**
         * Get account information
         */
        app.get("/api/account/*", ctx -> {
            Account userAccount = mongoManager.getAccount(ctx.splat(0));
            if (userAccount != null) {
                Document userAccountDoc = userAccount.toDoc(false); // False since we are sending it to the client, don't want to send pass
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

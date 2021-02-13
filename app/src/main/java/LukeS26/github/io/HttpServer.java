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
import LukeS26.github.io.dataschema.Profile;
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
        // insertTestProfile(); // Used for testing

        // #region Replies
        app.get("/api/replies/*", ctx -> {
            System.out.println("GET request for reply " + ctx.splat(0) + " from " + ctx.ip());

            FindIterable<Document> commentList = mongoManager.getReplies(ctx.splat(0));
            Document replyDoc = new Document("replies", commentList);
            ctx.result(replyDoc.toJson());
            ctx.status(HttpStatus.OK_200);
        });
        // #endregion

        // #region Comments
        app.post("/api/comments", ctx -> {
            System.out.println("POST request to comment from " + ctx.ip());
            Document doc = Document.parse(ctx.body());
            Comment comment = new Comment();

            // only accept ObjectId objects instead of strings to stay consistent, because I
            // am sending it through GETs in the same format
            comment.parentId = new ObjectId(doc.get("parent_id").toString());
            comment.author = (String) doc.get("author");
            comment.body = (String) doc.get("body");

            mongoManager.writeComment(comment);
            ctx.status(HttpStatus.CREATED_201);
        });

        app.get("/api/comments/*", ctx -> {
            System.out.println("GET request for comment " + ctx.splat(0) + " from " + ctx.ip());

            Comment comment = mongoManager.getComment(ctx.splat(0));
            if (comment != null) {
                String commentJson = comment.toDoc().toJson();

                ctx.result(commentJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                // You could provide an error body here
                ctx.status(HttpStatus.NOT_FOUND_404);
            }
        });
        // #endregion

        // #region Posts
        app.post("/api/posts", ctx -> {
            System.out.println("POST request to post from " + ctx.ip());
            Document doc = Document.parse(ctx.body());
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

        // #region Profiles/Accounts

        // User sends hashed password, salt is added to the end and then rehashed in
        // backend
        app.post("/api/account/signup", ctx -> {
            System.out.println("POST request to sign up from " + ctx.ip());
            System.out.println(ctx.body());
            Document doc = Document.parse(ctx.body());
            Profile userProfile = new Profile();

            /*
             * Can't use Profile.fromDoc here because this won't contain a bio, profile
             * link, permission id, etc.
             */
            userProfile.username = (String) doc.get("username");
            userProfile.firstName = (String) doc.get("first_name");
            userProfile.lastName = (String) doc.get("last_name");
            userProfile.email = (String) doc.get("email");
            userProfile.bio = null;
            userProfile.profilePictureLink = null;

            String receivedHash = (String) doc.get("password_hash");

            String salt = BCrypt.gensalt(Settings.BCRYPT_LOG_ROUNDS);
            String finalPasswordHash = BCrypt.hashpw(receivedHash, salt);

            userProfile.passwordHash = finalPasswordHash;
            userProfile.permissionID = 0;
            userProfile.badgeIDs = new ArrayList<Integer>();

            mongoManager.writeProfile(userProfile);

            ctx.status(HttpStatus.CREATED_201);
        });

        app.get("/api/profile/*", ctx -> {
            System.out.println("GET request for profile " + ctx.splat(0) + " from " + ctx.ip());
            Profile userProfile = mongoManager.getProfile(ctx.splat(0));
            if (userProfile != null) {
                Document userProfileDoc = userProfile.toDoc(false);
                String profileJson = userProfileDoc.toJson();

                ctx.result(profileJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                // You could provide an error body here
                ctx.status(HttpStatus.NOT_FOUND_404);
            }
        });
        // #endregion
    }

    public void testLogin(String username, String password) {
        Profile profile = mongoManager.getProfile(username);
        if (BCrypt.checkpw(password, profile.passwordHash)) {
            System.out.println("Password is a match");
        } else {
            System.out.println("Password is not a match");
        }
    }

    /**
     * Generates and adds a profile to the database containing test information
     */
    private void insertTestProfile() {
        List<Integer> badgeIDs = new ArrayList<>();
        badgeIDs.add(1);
        badgeIDs.add(5);
        badgeIDs.add(7);

        Profile p = new Profile();
        p.username = "JohnSmith72";
        p.passwordHash = "hash12345";
        p.firstName = "John";
        p.lastName = "Smith";
        p.email = "johnsmith@gmail.com";
        p.profilePictureLink = "https://LINK_TO_IMAGE/IMAGE_NAME.png";
        p.bio = "Example biography, this can be an empty string";
        p.permissionID = Utils.Permissions.USER.ordinal(); // Ordinal is index in enum
        p.badgeIDs = badgeIDs;

        mongoManager.writeProfile(p);
    }
}

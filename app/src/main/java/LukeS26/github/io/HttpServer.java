package LukeS26.github.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import com.mongodb.client.FindIterable;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jetty.http.HttpStatus;
import org.mindrot.jbcrypt.BCrypt;

import LukeS26.github.io.dataschema.Account;
import LukeS26.github.io.dataschema.Comment;
import LukeS26.github.io.dataschema.Post;
import LukeS26.github.io.dataschema.Token;
import io.javalin.Javalin;

public class HttpServer {
    public MongoManager mongoManager;
    private Javalin app;
    private String[] suspiciousEndpoints;
    private byte[] gzipBytes;

    public HttpServer() {
        System.out.println("Initializing MongoDB....");
        mongoManager = new MongoManager();
        System.out.println("Finished initializing MongoDB.");

        suspiciousEndpoints = new String[] { "client_area", "system_api", "GponForm", "stalker_portal", "manager/html",
                "stream/rtmp", "getuser?index=0", "jenkins/login", "check.best-proxies.ru", "setup.cgi", "script" };

        System.out.println("Initializing Javalin...");
        app = Javalin.create(config -> {
            config.requestLogger((ctx, ms) -> {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a");
                LocalDateTime now = LocalDateTime.now(ZoneId.of("US/Eastern"));
                System.out.println("[LOG] " + dtf.format(now) + " | " + ctx.method() + " request to " + ctx.fullUrl()
                        + " from userAgent: " + ctx.userAgent() + " and IP: " + ctx.ip());
            });

            config.enableCorsForAllOrigins();

        }).start(Settings.HTTP_SERVER_PORT);
        System.out.println("Finished initializing Javalin.");

        System.out.println("Loading GZip bomb...");
        try {
            gzipBytes = Files.readAllBytes(Paths.get(Settings.BOMB_LOCATION));

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished loading GZip bomb.");
    }

    public void start() {
        app.before(ctx -> {
            for (String s : suspiciousEndpoints) {
                if (ctx.fullUrl().contains(s)) {
                    // Put in try/catch in case they close the page while still sending the bomb
                    try {
                        ctx.header("Content-Encoding", "gzip");
                        ctx.header("Content-Length", "" + new File(Settings.BOMB_LOCATION).length());

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a");
                        LocalDateTime now = LocalDateTime.now(ZoneId.of("US/Eastern"));
                        System.out.println("[LOG] " + dtf.format(now) + " | " + ctx.method() + " request to "
                                + ctx.fullUrl() + " from userAgent: " + ctx.userAgent() + " and IP: " + ctx.ip());

                        System.out.println("[ANTI-BOT] Suspicious request to " + s + ". G-Zip bombing client...");

                        ServletOutputStream sos = ctx.res.getOutputStream();
                        sos.write(gzipBytes);
                        sos.flush();
                        return;

                    } catch (Exception e) {
                        System.out.println("Exception while sending GZip: " + e);
                    }
                }
            }
        });

        // #region Comments
        /**
         * Get all comments and comments on comments for a post
         */
        app.get("/api/comments/*", ctx -> {
            FindIterable<Document> commentList = mongoManager.findAllComments(ctx.splat(0));
            if (commentList == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                return;
            }

            Document replyDoc = new Document("comments", commentList);
            ctx.result(replyDoc.toJson());
            ctx.status(HttpStatus.OK_200);
        });

        /**
         * Create a comment for the specified parent
         */
        app.post("/api/comments", ctx -> {
            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!ctx.headerMap().containsKey("Authorization") || !doc.containsKey("post_id")
                    || !doc.containsKey("reply_to_id") || !doc.containsKey("body")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Document tokenDoc = mongoManager.findToken(ctx.header("Authorization"));
            if (tokenDoc == null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                return;
            }
            Token token = Token.fromDoc(tokenDoc);

            Comment comment = new Comment();
            // only accept ObjectId objects instead of strings to stay consistent, because I
            // am sending it through GETs in the same format
            comment.postId = new ObjectId((String) doc.get("post_id"));

            if (doc.get("reply_to_id") != null) {
                comment.replyToId = new ObjectId((String) doc.get("reply_to_id"));

            } else {
                comment.replyToId = null;
            }

            comment.author = token.username;
            comment.body = (String) doc.get("body");

            mongoManager.writeComment(comment);
            ctx.status(HttpStatus.CREATED_201);
        });

        /**
         * Get a specific comment
         */
        app.get("/api/comment/*", ctx -> {
            Document commentDoc = mongoManager.findComment(ctx.splat(0));
            if (commentDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                return;
            }
            String commentJson = commentDoc.toJson();

            ctx.result(commentJson);
            ctx.status(HttpStatus.OK_200);
        });
        // #endregion

        // #region Posts
        app.get("/api/posts/feed", ctx -> {
            int pageNum = 1;
            try {
                pageNum = Integer.parseInt(ctx.queryParam("page"));

            } catch (Exception e) {
            }

            System.out.println("Retrieving feed");
            FindIterable<Document> feed = mongoManager.getFeed(pageNum);
            Document feedDoc = new Document("feed", feed);

            ctx.result(feedDoc.toJson());
            ctx.status(HttpStatus.OK_200);
        });

        /**
         * Create a post
         */
        app.post("/api/posts", ctx -> {
            ctx.header("Access-Control-Allow-Headers", "Authorization");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.header("Access-Control-Allow-Origin", "http://" + Settings.WEBSITE_URL);

            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!ctx.headerMap().containsKey("Authorization") || !doc.containsKey("title")
                    || !doc.containsKey("body")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Document tokenDoc = mongoManager.findToken(ctx.header("Authorization"));
            if (tokenDoc == null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                return;
            }
            Token token = Token.fromDoc(tokenDoc);

            Post post = new Post(); // Can't use Post.fromDoc because it doesn't contain an ID here
            post.author = token.username;
            post.title = (String) doc.get("title");
            post.body = format((String) doc.get("body"));

            post.date = new Date();

            mongoManager.writePost(post);

            ctx.status(HttpStatus.CREATED_201);
        });

        /**
         * Get a post
         */
        app.get("/api/posts/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://" + Settings.WEBSITE_URL);

            Document postDoc = mongoManager.findPost(ctx.splat(0));
            if (postDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                return;
            }

            String postJson = postDoc.toJson();

            ctx.result(postJson);
            ctx.status(HttpStatus.OK_200);
        });

        app.delete("/api/posts/*", ctx -> {
            ctx.header("Access-Control-Allow-Headers", "Authorization");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.header("Access-Control-Allow-Origin", "http://" + Settings.WEBSITE_URL);

            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Document tokenDoc = mongoManager.findToken(ctx.header("Authorization"));
            if (tokenDoc == null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                return;
            }
            Token token = Token.fromDoc(tokenDoc);

            Document post = mongoManager.findPost(ctx.splat(0));
            if (!((String) post.get("author")).equals(token.username)) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                return;
            }

            mongoManager.deletePost(post);

            ctx.status(HttpStatus.OK_200);
        });
        // #endregion

        // #region Accounts
        app.patch("/api/account", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://" + Settings.WEBSITE_URL);

            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Document tokenDoc = mongoManager.findToken(ctx.header("Authorization"));
            if (tokenDoc == null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                return;
            }

            Document changes = new Document();
            // Create an empty account just for checking if the key exists
            Document blankAccount = new Account().toDoc(true);
            for (Entry<String, Object> e : doc.entrySet()) {
                // Check if the empty account has the given key before setting it
                if (!blankAccount.containsKey(e.getKey())) {
                    ctx.status(HttpStatus.BAD_REQUEST_400);
                    return;
                }

                // Checking if using a link to an image
                // TODO: Certain links don't work, removing for now
                /*if (e.getKey().equals("profile_picture_link")) {
                    // Checks for http:// or https://, has to be a valid site and have a file extension at the end (https://site.com/image.png)
                    // Since its checking an unlimited amount of characters for the site name, it could be www.site.com or just site.com, it doesn't matter
                    
                    Pattern p = Pattern.compile("^http[s]{0,1}:\\/\\/.*\\/.*\\.[a-zA-Z]{3,4}");
                    Matcher m = p.matcher((String) e.getValue());
                    if (!m.find()) {
                        ctx.status(HttpStatus.BAD_REQUEST_400);
                        return;
                    }
                }*/


                // TODO: Check if setting profile pic link to something besides a link, etc.

                // Checking if the username already exists
                if (e.getKey().equals("username")) {
                    Document existingUserDoc = mongoManager.findAccount((String) e.getValue());
                    if (existingUserDoc != null) {
                        ctx.status(HttpStatus.FORBIDDEN_403);
                        return;
                    }
                }
                changes.put(e.getKey(), e.getValue());
            }

            mongoManager.updateAccount((String) tokenDoc.get("username"), changes);
            ctx.status(HttpStatus.NO_CONTENT_204); // Used when not responding with content but it was successful
        });

        /**
         * Create a new account
         */
        app.post("/api/account/signup", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://" + Settings.WEBSITE_URL);

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

            // Checking if account already exists with that username
            Document accountDoc = mongoManager.findAccount((String) doc.get("username"));
            if (accountDoc != null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                return;
            }
            Account userAccount = new Account();
            /*
             * Can't use .fromDoc here because this won't contain a bio, userAccount link,
             * permission id, etc.
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

            Document tokenDoc = mongoManager.findTokenForUser(userAccount.username);
            String tokenJson;
            if (tokenDoc != null) {
                tokenDoc.remove("_id");
                tokenJson = tokenDoc.toJson();

            } else {
                Token token = new Token(userAccount.username);
                System.out.println("Writing token");
                mongoManager.writeToken(token);
                tokenJson = token.toDoc().toJson();
            }

            ctx.result(tokenJson);
            ctx.status(HttpStatus.CREATED_201);
        });

        /**
         * Get account information
         */
        app.get("/api/account/*", ctx -> {
            Document existingUserAccountDoc = mongoManager.findAccount(ctx.splat(0));
            if (existingUserAccountDoc != null) {
                Account userAccount = Account.fromDoc(existingUserAccountDoc);
                Document userAccountDoc = userAccount.toDoc(false); // False since we are sending it to the client,
                                                                    // don't want to send pass
                String accountJson = userAccountDoc.toJson();

                ctx.result(accountJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                // You could provide an error body here
                ctx.status(HttpStatus.NOT_FOUND_404);
            }
        });

        /**
         * Getting an account token + verifying username+pass
         */
        app.post("/api/account/login", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "http://" + Settings.WEBSITE_URL);

            Document doc = null;
            try {
                doc = Document.parse(ctx.body());

            } catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            if (!doc.containsKey("username") || !doc.containsKey("password")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            Document loginAccountDoc = mongoManager.findAccount((String) doc.get("username"));
            if (loginAccountDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                return;
            }
            Account loginAccount = Account.fromDoc(loginAccountDoc);

            System.out.println("Found account");

            if (BCrypt.checkpw((String) doc.get("password"), loginAccount.passwordHash)) {
                System.out.println("Correct password");

                Document tokenDoc = mongoManager.findTokenForUser((String) doc.get("username"));
                tokenDoc.remove("_id");
                String tokenJson = tokenDoc.toJson();

                ctx.result(tokenJson);
                ctx.status(HttpStatus.OK_200);

            } else {
                ctx.status(HttpStatus.FORBIDDEN_403);
            }

        });
        // #endregion
    }

    public static String format(String str) {
        str.replace("<", "&lt;");
        str.replace(">", "&gt;");

        return str;
    }
}
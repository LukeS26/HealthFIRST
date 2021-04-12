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
import java.util.Objects;
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
import io.javalin.Javalin;

public class HttpServer {
	private static HttpServer instance;
	public MongoManager mongoManager;
	private final Javalin app;
	private final String[] suspiciousEndpoints;
	private byte[] gzipBytes;

	public static HttpServer getInstance() {
		if (instance == null) {
			instance = new HttpServer();
		}

		return instance;
	}

	private HttpServer() {
		System.out.println("Initializing MongoDB....");
		mongoManager = MongoManager.getInstance();
		System.out.println("Finished initializing MongoDB.");

		suspiciousEndpoints = new String[]{"client_area", "system_api", "GponForm", "stalker_portal", "manager/html",
				"stream/rtmp", "getuser?index=0", "jenkins/login", "check.best-proxies.ru", "setup.cgi", "script"};

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
		// Checking for suspicious requests
		app.before(ctx -> {
			for(String s : suspiciousEndpoints) {
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
		// #region Challenges
		// Complete a challenge
		app.post("/api/challenges/complete/*", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION);
				return;
			}

			try {
				mongoManager.completeChallenge(Integer.parseInt(Objects.requireNonNull(ctx.splat(0))), userAccount);
				ctx.status(HttpStatus.NO_CONTENT_204);

			} catch (NullPointerException e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_URL);
			}
		});

		app.get("/api/challenges", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document challengeDoc = mongoManager.findCurrentChallenge();
			challengeDoc.remove("_id");

			ctx.result(challengeDoc.toJson());
			ctx.status(HttpStatus.OK_200);
		});

		app.get("/api/challenges/feed", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			int pageNum = 1;
			try {
				pageNum = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("page")));

			} catch (Exception ignored) {
			}

			FindIterable<Document> feed = mongoManager.getChallengeFeed(pageNum);
			Document feedDoc = new Document("feed", feed);

			ctx.result(feedDoc.toJson());
			ctx.status(HttpStatus.OK_200);
		});
		// #endregion

		// #region Comments
		// Edit a comment
		app.patch("/api/comments/*", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (doc.get("body") == null) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			if (((String) doc.get("body")).length() > Settings.MAX_COMMENT_BODY_LENGTH) {
                ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
                ctx.result(Utils.BODY_TOO_LONG);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Document commentDoc = mongoManager.findComment(ctx.splat(0));
			if (!(format((String) commentDoc.get("author"))).equals(userAccount.username)
					&& userAccount.permissionID != Utils.Permissions.MODERATOR.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION);
				return;
			}

			// Recreating document with just a body so that it can't contain other fields
			// (author, date, etc.)
			mongoManager.editComment((ObjectId) commentDoc.get("_id"), new Document("body", doc.get("body")));

			ctx.status(HttpStatus.NO_CONTENT_204);
		});

		// Delete a comment
		app.delete("/api/comments/*", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			try {
				Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION);
				return;
			}

			Document commentDoc = mongoManager.findComment(ctx.splat(0));
			if (!(format((String) commentDoc.get("author"))).equals(userAccount.username)
					&& userAccount.permissionID != Utils.Permissions.MODERATOR.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION);
				return;
			}

			mongoManager.deleteComment((String) commentDoc.get("_id"));

			ctx.status(HttpStatus.NO_CONTENT_204);
		});

		// Get all comments and comments on comments for a post
		app.get("/api/comments/*", ctx -> {
			FindIterable<Document> commentList = mongoManager.findAllComments(ctx.splat(0));
			if (commentList == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}

			Document replyDoc = new Document("comments", commentList);
			ctx.result(replyDoc.toJson());
			ctx.status(HttpStatus.OK_200);
		});

		// Create a comment for the specified parent
		app.post("/api/comments", ctx -> {
			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!doc.containsKey("post_id") || !doc.containsKey("body")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.MISSING_BODY_VALUES);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			if (((String) doc.get("body")).length() > Settings.MAX_COMMENT_BODY_LENGTH) {
                ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
                ctx.result(Utils.BODY_TOO_LONG);
				return;
            }
            
            if (((String) doc.get("body")).length() == 0) {
				ctx.status(HttpStatus.BAD_REQUEST_400);
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Comment comment = new Comment();
			// only accept ObjectId objects instead of strings to stay consistent, because I
			// am sending it through GETs in the same format
			comment.postId = new ObjectId((String) doc.get("post_id"));

			if (doc.get("reply_to_id") != null) {
				comment.replyToId = (ObjectId) doc.get("reply_to_id");

			} else {
				comment.replyToId = null;
			}

			comment.author = format(userAccount.username);
			comment.body = format((String) doc.get("body"));
			comment.date = new Date();
			Document commentDoc = comment.toDoc();

			mongoManager.writeCommentDoc(commentDoc);
			ctx.status(HttpStatus.CREATED_201);
			ctx.result(((ObjectId) commentDoc.get("_id")).toString());
		});

		// Get a specific comment
		app.get("/api/comment/*", ctx -> {
			Document commentDoc = mongoManager.findComment(ctx.splat(0));
			if (commentDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}
			String commentJson = commentDoc.toJson();

			ctx.result(commentJson);
			ctx.status(HttpStatus.OK_200);
		});
		// #endregion

		// #region Posts
		// Get a post feed offset by the page number
		app.get("/api/posts/feed", ctx -> {
			int pageNum = 1;
			try {
				pageNum = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("page")));

			} catch (Exception ignored) {
			}

			FindIterable<Document> feed = mongoManager.getFeed(pageNum);
			Document feedDoc = new Document("feed", feed);

			ctx.result(feedDoc.toJson());
			ctx.status(HttpStatus.OK_200);
		});

		// Edit a post
		app.patch("/api/post/*", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
				ctx.status(HttpStatus.BAD_REQUEST_400);
				ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (doc.get("title") == null && doc.get("body") == null) {
				ctx.status(HttpStatus.BAD_REQUEST_400);
				ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
				ctx.status(HttpStatus.UNAUTHORIZED_401);
				ctx.result(Utils.NO_TOKEN);
				return;
			}

			if (((String) doc.get("body")).length() > Settings.MAX_COMMENT_BODY_LENGTH) {
				ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
				ctx.result(Utils.BODY_TOO_LONG);
				return;
			}

			if (((String) doc.get("title")).length() > Settings.MAX_POST_TITLE_LENGTH) {
				ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
				ctx.result(Utils.BODY_TOO_LONG);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
				ctx.status(HttpStatus.UNAUTHORIZED_401);
				ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
				ctx.status(HttpStatus.FORBIDDEN_403);
				ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Document postDoc = mongoManager.findPost(ctx.splat(0));
			if (!(format((String) postDoc.get("author"))).equals(userAccount.username)
					&& userAccount.permissionID != Utils.Permissions.MODERATOR.ordinal()) {
				ctx.status(HttpStatus.FORBIDDEN_403);
				ctx.result(Utils.NO_PERMISSION);
				return;
			}

			// Recreating document with just a body so that it can't contain other fields
			// (author, date, etc.)
			mongoManager.editPost((ObjectId) postDoc.get("_id"), new Document("title", doc.get("title")).append("body", doc.get("body")));

			ctx.status(HttpStatus.NO_CONTENT_204);
		});

		// Create a post
		app.post("/api/posts", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!doc.containsKey("title") || !doc.containsKey("body")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.MISSING_BODY_VALUES);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			if (((String) doc.get("title")).length() > Settings.MAX_POST_TITLE_LENGTH) {
                ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
                ctx.result(Utils.TITLE_TOO_LONG);
				return;
			}

			if (((String) doc.get("body")).length() > Settings.MAX_POST_BODY_LENGTH) {
                ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
                ctx.result(Utils.BODY_TOO_LONG);
				return;
			}

			if (((String) doc.get("title")).length() == 0 || ((String) doc.get("body")).length() == 0) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.BLANK_FIELD_SUBMITTED);
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Post post = new Post(); // Can't use Post.fromDoc because it doesn't contain an ID here
			post.author = format(userAccount.username);
			post.title = format((String) doc.get("title"));
			post.body = format((String) doc.get("body"));

			post.date = new Date();

			Document postDoc = post.toDoc();
			mongoManager.writePostDoc(postDoc);

			ctx.result(((ObjectId) postDoc.get("_id")).toString());
			ctx.status(HttpStatus.CREATED_201);
		});

		// Get a post
		app.get("/api/posts/*", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document postDoc = mongoManager.findPost(ctx.splat(0));
			if (postDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}

			String postJson = postDoc.toJson();

			ctx.result(postJson);
			ctx.status(HttpStatus.OK_200);
		});

		// Delete a post
		app.delete("/api/posts/*", ctx -> {
			ctx.header("Access-Control-Allow-Headers", "Authorization");
			ctx.header("Access-Control-Allow-Credentials", "true");
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Document post = mongoManager.findPost(ctx.splat(0));
			if (!(format((String) post.get("author"))).equals(userAccount.username)
					&& userAccount.permissionID != Utils.Permissions.MODERATOR.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION);
				return;
			}

			mongoManager.deletePost(post);
			ctx.status(HttpStatus.NO_CONTENT_204);
		});
		// #endregion

		// #region Accounts
		// Update an account
		app.patch("/api/account", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Document changes = new Document();
			// Create an empty account just for checking if the key exists
			Document blankAccount = new Account().toDoc(true);
			for(Entry<String, Object> e : doc.entrySet()) {
				// Check if the empty account has the given key before setting it
				if (!blankAccount.containsKey(e.getKey())) {
                    ctx.status(HttpStatus.BAD_REQUEST_400);
                    ctx.result(Utils.CANNOT_EDIT_FIELD);
					return;
				}

				if (e.getKey().equals("username") || e.getKey().equals("permission_id")
						|| e.getKey().equals("badge_ids")) {
					continue;
				}

				changes.put(e.getKey(), e.getValue());
			}

			mongoManager.updateAccount(userAccount.username, changes);
			ctx.status(HttpStatus.NO_CONTENT_204);
		});

		// Delete an account
		app.delete("/api/account/*", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccount(ctx.splat(0), false));
			Account tokenAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			// if the deleter or the account to be deleted are null || you aren't deleting
			// your own account and you aren't an admin
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
            }
            
            if (tokenAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			if (!userAccount.username.equals(tokenAccount.username)
					&& tokenAccount.permissionID != Utils.Permissions.MODERATOR.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION);
				return;
			}

			mongoManager.deleteAccount(userAccount.username);
			ctx.status(HttpStatus.NO_CONTENT_204);
		});

		// Create a new account
		app.post("/api/account/signup", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!doc.containsKey("username") || !doc.containsKey("first_name") || !doc.containsKey("last_name")
					|| !doc.containsKey("email") || !doc.containsKey("password_hash")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.MISSING_BODY_VALUES);
				return;
			}

			Pattern p = Pattern.compile("[^0-9a-zA-Z]+");
			Matcher m = p.matcher((String) doc.get("username"));
			if (m.find()) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_USERNAME_CHARACTERS);
				return;
			}

			if (((String) doc.get("username")).length() > Settings.MAX_USERNAME_LENGTH) {
                ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
                ctx.result(Utils.USERNAME_TOO_LONG);
				return;
			}

			if (((String) doc.get("password_hash")).length() > Settings.MAX_PASSWORD_LENGTH) {
				ctx.status(HttpStatus.PAYLOAD_TOO_LARGE_413);
				ctx.result(Utils.PASSWORD_TOO_LONG);
			}

			// Checking if account already exists with that username
			Document accountDoc = mongoManager.findAccount((String) doc.get("username"), false);
			if (accountDoc != null) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.DUPLICATE_USERNAME);
				return;
			}
			Account userAccount = new Account();
			/*
			 * Can't use .fromDoc here because this won't contain a bio, userAccount link,
			 * permission id, etc.
			 */
			userAccount.username = format((String) doc.get("username"));
			userAccount.firstName = format((String) doc.get("first_name"));
			userAccount.lastName = format((String) doc.get("last_name"));
			userAccount.email = format((String) doc.get("email"));
			userAccount.bio = null;
			userAccount.profilePictureLink = null;

			String receivedHash = (String) doc.get("password_hash");

			String salt = BCrypt.gensalt(Settings.BCRYPT_LOG_ROUNDS);
			userAccount.passwordHash = BCrypt.hashpw(receivedHash, salt);
			userAccount.token = Account.generateToken();

			userAccount.permissionID = Utils.Permissions.USER.ordinal();
			userAccount.badgeIDs = new ArrayList<>();
			userAccount.following = new ArrayList<>();

			userAccount.signupDate = new Date();

			mongoManager.writeAccount(userAccount);

			ctx.result(new Document("token", userAccount.token).toJson());
			ctx.status(HttpStatus.CREATED_201);
		});

		app.get("/api/account/*/posts", ctx -> {
			Document existingUserAccountDoc = mongoManager.findAccount(ctx.splat(0), false);
			if (existingUserAccountDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}

			FindIterable<Document> posts = mongoManager.findPostsForUser(ctx.splat(0));
			Document postsDoc = new Document("posts", posts);

			ctx.result(postsDoc.toJson());
			ctx.status(HttpStatus.OK_200);
		});

		// Get account information
		app.get("/api/account/*", ctx -> {
			Document existingUserAccountDoc = mongoManager.findAccount(ctx.splat(0), false);
			if (existingUserAccountDoc == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}

			Account userAccount = Account.fromDoc(existingUserAccountDoc);
			Document userAccountDoc = userAccount.toDoc(false); // False since we are sending it to the client,
			// don't want to send pass
			String accountJson = userAccountDoc.toJson();

			ctx.result(accountJson);
			ctx.status(HttpStatus.OK_200);
		});

		// Get more sensitive account information
		app.get("/api/account", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Document accountDoc = mongoManager.findAccountByToken(ctx.header("Authorization"));
			if (accountDoc == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			accountDoc.remove("_id");
			accountDoc.remove("password_hash");
			accountDoc.remove("token");

			ctx.result(accountDoc.toJson());
			ctx.status(HttpStatus.OK_200);
		});

		// Getting an account token + verifying username+pass
		app.post("/api/account/login", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			Document doc;
			try {
				doc = Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!doc.containsKey("username") || !doc.containsKey("password")) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.MISSING_BODY_VALUES);
				return;
			}

			Document loginAccountDoc = mongoManager.findAccount(format((String) doc.get("username")), true);
			if (loginAccountDoc == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.USERNAME_ACCOUNT_DOESNT_EXIST);
				return;
			}
			Account loginAccount = Account.fromDoc(loginAccountDoc);

			if (BCrypt.checkpw((String) doc.get("password"), loginAccount.passwordHash)) {
				Document responseDoc = new Document("token", loginAccount.token).append("username",
						loginAccount.username);
				ctx.result(responseDoc.toJson());
				ctx.status(HttpStatus.OK_200);

			} else {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.INCORRECT_PASSWORD);
			}
		});

		// Verify that a token is linked to an account
		app.post("/api/token/verify", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			try {
				Document.parse(ctx.body());

			} catch (Exception e) {
                ctx.status(HttpStatus.BAD_REQUEST_400);
                ctx.result(Utils.INVALID_JSON);
				return;
			}

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount != null) {
                ctx.status(HttpStatus.NO_CONTENT_204);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

            ctx.status(HttpStatus.FORBIDDEN_403);
            ctx.result(Utils.INVALID_TOKEN);
		});
		// #endregion

		// #region Followers
		app.post("/api/following/*", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Account followAccount = Account.fromDoc(mongoManager.findAccount(ctx.splat(0), false));
			if (followAccount == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}

			if (userAccount.following.contains(ctx.splat(0))) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.ALREADY_FOLLOWING);
				return;
            }

			userAccount.following.add(ctx.splat(0));
			Document updateDoc = new Document("following", userAccount.following);
			mongoManager.updateAccount(userAccount.username, updateDoc);
			ctx.status(HttpStatus.NO_CONTENT_204);
		});

		app.delete("/api/following/*", ctx -> {
			ctx.header("Access-Control-Allow-Origin", Settings.WEBSITE_URL);

			if (!ctx.headerMap().containsKey("Authorization")) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.NO_TOKEN);
				return;
			}

			Account userAccount = Account.fromDoc(mongoManager.findAccountByToken(ctx.header("Authorization")));
			if (userAccount == null) {
                ctx.status(HttpStatus.UNAUTHORIZED_401);
                ctx.result(Utils.TOKEN_ACCOUNT_DOESNT_EXIST);
				return;
			}

			if (userAccount.permissionID == Utils.Permissions.BANNED.ordinal()) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NO_PERMISSION_BANNED);
				return;
			}

			Account unFollowAccount = Account.fromDoc(mongoManager.findAccount(ctx.splat(0), false));
			if (unFollowAccount == null) {
                ctx.status(HttpStatus.NOT_FOUND_404);
                ctx.result(Utils.RESOURCE_DOESNT_EXIST);
				return;
			}

			if (!userAccount.following.contains(ctx.splat(0))) {
                ctx.status(HttpStatus.FORBIDDEN_403);
                ctx.result(Utils.NOT_FOLLOWING);
				return;
			}

			userAccount.following.remove(ctx.splat(0));
			Document updateDoc = new Document("following", userAccount.following);
			mongoManager.updateAccount(userAccount.username, updateDoc);
			ctx.status(HttpStatus.NO_CONTENT_204);
		});
		// #endregion
	}

	/**
	 * Replacing characters to prevent escaping strings in a post title, body, etc.
	 */
	public String format(String str) {
		str = str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("=", "&#61;")
				.replace(":", "&#58;").replace("\"", "&#34;");

		return str;
	}
}
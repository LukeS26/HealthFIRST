package LukeS26.github.io;

public class Settings {
    public static final String WEBSITE_URL = "";
	public static final String WEBSITE_IP = "";
	public static final int MONGO_PORT = 0;
	public static final String MONGO_ADMIN_USERNAME = "";
	public static final String MONGO_ADMIN_PASSWORD = "";
	public static final String MONGO_URI = "";

	public static final String MONGO_DATABASE_NAME = "";
	public static final String ACCOUNTS_COLLECTION_NAME = "";
	public static final String POSTS_COLLECTION_NAME = "";
	public static final String COMMENTS_COLLECTION_NAME = "";
	public static final String CHALLENGES_COLLECTION_NAME = "";
	public static final String CONFIRMATION_KEY_COLLECTION_NAME = "";

	public static final int MAX_USERNAME_LENGTH = 0;
	public static final int MAX_PASSWORD_LENGTH = 0;
	public static final int MAX_POST_TITLE_LENGTH = 0;
	public static final int MAX_POST_BODY_LENGTH = 0;
	public static final int MAX_COMMENT_BODY_LENGTH = 0;

	// Values are in requests per minute
	public static final int COMPLETE_CHALLENGE_RATELIMIT = 60;
	public static final int GET_CURRENT_CHALLENGE_RATELIMIT = 60;
	public static final int GET_CHALLENGES_FEED_RATELIMIT = 60;
	public static final int EDIT_COMMENT_RATELIMIT = 2;
	public static final int DELETE_COMMENT_RATELIMIT = 2;
	public static final int GET_COMMENT_REPLIES_RATELIMIT = 60;
	public static final int CREATE_COMMENT_RATELIMIT = 1;
	public static final int GET_COMMENT_RATELIMIT = 60;
	public static final int GET_POST_FEED_RATELIMIT = 60;
	public static final int EDIT_POST_RATELIMIT = 1;
	public static final int CREATE_POST_RATELIMIT = 2;
	public static final int GET_POST_RATELIMIT = 60;
	public static final int DELETE_POST_RATELIMIT = 2;
	public static final int UPDATE_ACCOUNT_RATELIMIT = 5;
	public static final int DELETE_ACCOUNT_RATELIMIT = 5;
	public static final int CREATE_ACCOUNT_RATELIMIT = 10;
	public static final int GET_ACCOUNT_POSTS_RATELIMIT = 60;
	public static final int GET_ACCOUNT_INFORMATION_RATELIMIT = 30;
	public static final int GET_ACCOUNT_SENSITIVE_INFORMATION_RATELIMIT = 30;
	public static final int LOGIN_RATELIMIT = 2;
	public static final int VERIFY_TOKEN_RATELIMIT = 1;
	public static final int FOLLOW_RATELIMIT = 30;
	public static final int UNFOLLOW_RATELIMIT = 30;

	public static final int HTTP_SERVER_PORT = 0;

	public static final int BCRYPT_LOG_ROUNDS = 0;

	public static final String BOMB_LOCATION = "";

	public static final int POSTS_PER_PAGE = 0;
	public static final int CHALLENGES_PER_PAGE = 0;
}

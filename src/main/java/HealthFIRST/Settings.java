package HealthFIRST;

public class Settings {
	//region Domain Settings
	public static final String WEBSITE_URL = "http://healthfirst4342.tk";
	public static final String WEBSITE_IP = "157.230.233.218";
	//endregion

	//region MongoDB
	public static final int MONGO_PORT = 0;
	public static final String MONGO_ADMIN_USERNAME = "";
	public static final String MONGO_ADMIN_PASSWORD = "";
	public static final String MONGO_URI = "";
	public static final String MONGO_DATABASE_NAME = "InnovationApp";
	public static final String ACCOUNTS_COLLECTION_NAME = "Accounts";
	public static final String POSTS_COLLECTION_NAME = "Posts";
	public static final String COMMENTS_COLLECTION_NAME = "Comments";
	public static final String CHALLENGES_COLLECTION_NAME = "Challenges";
	public static final String CONFIRMATION_KEY_COLLECTION_NAME = "ConfirmationKeys";
	//endregion

	//region Max Character Lengths
	public static final int MAX_USERNAME_LENGTH = 20;
	public static final int MAX_PASSWORD_LENGTH = 50;
	public static final int MAX_POST_TITLE_LENGTH = 40;
	public static final int MAX_POST_BODY_LENGTH = 3000;
	public static final int MAX_COMMENT_BODY_LENGTH = 1500;
	//endregion

	//region Rate Limiting
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
	//endregion

	//region Misc
	public static final int HTTP_SERVER_PORT = 8080;

	public static final int BCRYPT_LOG_ROUNDS = 0;

	public static final String BOMB_LOCATION = "";
	//endregion

	//region Resources per Page
	public static final int POSTS_PER_PAGE = 10;
	public static final int CHALLENGES_PER_PAGE = 5;
	//endregion

	//region SMTP
	public static final String EMAIL = "healthfirst4342@gmail.com";
	public static final String SMTP_URL = "smtp.gmail.com";
	public static final String EMAIL_PASSWORD = "";
	public static final String SMTP_PORT = "587";

	//endregion
}

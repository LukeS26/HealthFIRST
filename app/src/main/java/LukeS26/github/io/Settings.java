package LukeS26.github.io;

public class Settings {
   public static final String WEBSITE_URL = "157.230.233.218";
   public static final int MONGO_PORT = 27017;
   public static final String MONGO_ADMIN_USERNAME = "admin";
   public static final String MONGO_ADMIN_PASSWORD = ")(8_wS@~8ftNy5$6G$g*:#FH$]2t`p$$<yHn6U)uu.!Fy)Q:!`";
   public static final String MONGO_URI = "mongodb://" + MONGO_ADMIN_USERNAME + ":" + MONGO_ADMIN_PASSWORD + "@157.230.233.218:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";

   public static final String MONGO_DATABASE_NAME = "InnovationApp";
   public static final String ACCOUNTS_COLLECTION_NAME = "Accounts";
   public static final String POSTS_COLLECTION_NAME = "Posts";
   public static final String COMMENTS_COLLECTION_NAME = "Comments";
   public static final String TOKENS_COLLECTION_NAME = "Tokens";

   public static final int HTTP_SERVER_PORT = 8080;

   public static final int BCRYPT_LOG_ROUNDS = 10;
}

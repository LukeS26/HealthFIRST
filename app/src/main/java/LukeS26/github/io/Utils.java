package LukeS26.github.io;

import java.util.Dictionary;

public class Utils {
    public enum Permissions {
        BANNED, USER, MODERATOR
    }

    public static String NO_TOKEN = "No token sent, please make sure you are logged in.";
    public static String TOKEN_ACCOUNT_DOESNT_EXIST = "An account with the supplied token doesn't exist.";
    public static String NO_PERMISSION = "You do not have permission to complete that action.";
    public static String NO_PERMISSION_BANNED = "You do not have permission to complete that action. (You have been banned)";
    public static String INVALID_URL = "Invalid URL (missing or malformed information)";
    public static String INVALID_JSON = "Invalid JSON submitted.";
    public static String MISSING_BODY_VALUES = "Missing values in body";
    public static String BODY_TOO_LONG = "Submitted body is too long. Body must be less than " + Settings.MAX_POST_BODY_LENGTH + " characters";
    public static String TITLE_TOO_LONG = "Submitted title is too long. Title must be less than " + Settings.MAX_POST_TITLE_LENGTH + " characters";
    public static String USERNAME_TOO_LONG = "Submitted username is too long. Username must be less than " + Settings.MAX_USERNAME_LENGTH + " characters";
    public static String RESOURCE_DOESNT_EXIST = "Request resource doesn't exist";
    public static String BLANK_FIELD_SUBMITTED = "A submitted field was left blank.";
    public static String CANNOT_EDIT_FIELD = "A submitted field cannot be edited.";
    public static String INVALID_USERNAME_CHARACTERS = "A username must be alphanumeric (A-Z, 0-9)";
    public static String DUPLICATE_USERNAME = "An account with that username already exists.";
    public static String INCORRECT_PASSWORD = "Incorrect password.";
    public static String INVALID_TOKEN = "Invalid token used.";
    public static String ALREADY_FOLLOWING = "You are already following this user.";
    public static String NOT_FOLLOWING = "You are not following this user."; 
}

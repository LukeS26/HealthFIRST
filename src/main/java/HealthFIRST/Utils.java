package HealthFIRST;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@SuppressWarnings("SpellCheckingInspection")
public class Utils {
	public enum Permissions {
		BANNED, UNCONFIRMED, USER, MODERATOR
	}

	public static final String TOKEN_ACCOUNT_DOESNT_EXIST = "An account with the supplied token doesn't exist.";
	public static final String USERNAME_ACCOUNT_DOESNT_EXIST = "An account with that username doesn't exist.";
	public static final String RESOURCE_DOESNT_EXIST = "Request resource doesn't exist";
	public static final String CONFIRMATION_KEY_DOESNT_EXIST = "Invalid confirmation key. Try resending the confirmation email.";
	public static final String NO_TOKEN = "No token sent, please make sure you are logged in.";
	public static final String NO_PERMISSION = "You do not have permission to complete that action.";
	public static final String NO_PERMISSION_BANNED = "You do not have permission to complete that action. (You have been banned)";
	public static final String INVALID_URL = "Invalid URL (missing or malformed information)";
	public static final String INVALID_JSON = "Invalid JSON submitted.";
	public static final String MISSING_BODY_VALUES = "Missing values in body";
	public static final String BODY_TOO_LONG = "Submitted body is too long. Body must be less than " + Settings.MAX_POST_BODY_LENGTH + " characters.";
	public static final String TITLE_TOO_LONG = "Submitted title is too long. Title must be less than " + Settings.MAX_POST_TITLE_LENGTH + " characters.";
	public static final String USERNAME_TOO_LONG = "Submitted username is too long. Username must be less than " + Settings.MAX_USERNAME_LENGTH + " characters.";
	public static final String PASSWORD_TOO_LONG = "Submitted password is too long. Password must be less than " + Settings.MAX_PASSWORD_LENGTH + " characters.";
	public static final String BLANK_FIELD_SUBMITTED = "A submitted field was left blank.";
	public static final String CANNOT_EDIT_FIELD = "A submitted field cannot be edited.";
	public static final String INVALID_USERNAME_CHARACTERS = "A username must be alphanumeric (A-Z, 0-9)";
	public static final String DUPLICATE_USERNAME = "An account with that username already exists.";
	public static final String INCORRECT_PASSWORD = "Incorrect password.";
	public static final String INVALID_TOKEN = "Invalid token used.";
	public static final String ALREADY_FOLLOWING = "You are already following this user.";
	public static final String NOT_FOLLOWING = "You are not following this user.";
	public static final String ACCOUNT_NOT_CONFIRMED = "Please confirm your email before trying to complete this action.";

	public static String generateRandomConfirmationKey() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			SecureRandom sr = new SecureRandom();
			byte[] randomBytes = new byte[1024];

			sr.nextBytes(randomBytes);

			byte[] hashBytes = md.digest(randomBytes);

			StringBuilder sb = new StringBuilder(hashBytes.length * 2);
			for (byte b : hashBytes) {
				int value = 0xFF & b;
				String toAppend = Integer.toHexString(value);
				sb.append(toAppend);
			}
			sb.setLength(sb.length() - 1);
			return sb.toString();

		} catch (NoSuchAlgorithmException ignored) {
			return null;
		}
	}
}

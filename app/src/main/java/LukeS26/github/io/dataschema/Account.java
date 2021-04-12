package LukeS26.github.io.dataschema;

import LukeS26.github.io.Settings;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Used for interaction with MongoDB
 */
public class Account extends DataSchema {
    public String username;
    public String email;
    public String passwordHash;
    public String token;

    public String firstName;
    public String lastName;

    public String profilePictureLink;
    public String bio; // Can be null

    public int permissionID;
    public List<Integer> badgeIDs; // Can be null/empty
    public List<String> following;

    public Date signupDate;

    /**
     * Convert the Account object to a org.bson.Document
     * 
     * @param includeSecrets whether to include the password salt/hash and token in the
     *                        account (false when sending the account to the
     *                        frontend, true when working when account in the
     *                        backend)
     * @return account formatted into a Document
     */
    public Document toDoc(boolean includeSecrets) {
        Document userDoc = new Document("username", username).append("biography", bio)
                .append("profile_picture_link", profilePictureLink);

        if (includeSecrets) {
            userDoc.append("password_hash", passwordHash);
            userDoc.append("token", token);
            userDoc.append("first_name", firstName);
            userDoc.append("last_name", lastName);
            userDoc.append("email", email);
            userDoc.append("following", following);
        }

        userDoc.append("permission_id", permissionID).append("badge_ids", badgeIDs).append("signup_date", signupDate);

        return userDoc;
    }

    public static String generateToken() {
        String salt = BCrypt.gensalt(Settings.BCRYPT_LOG_ROUNDS);
        return BCrypt.hashpw(UUID.randomUUID().toString(), salt);
    }

    /**
     * Converts account to doc, assuming you want to show password
     * 
     * @return account formatted into a Document
     */
    @Override
    public Document toDoc() {
        return new Document("username", username).append("first_name", firstName)
                .append("last_name", lastName).append("email", email).append("token", token).append("biography", bio)
                .append("profile_picture_link", profilePictureLink).append("password_hash", passwordHash)
                .append("permission_id", permissionID).append("badge_ids", badgeIDs).append("following", following).append("signup_date", signupDate);
    }

    /**
     * Parse a document into a account
     * 
     * @param doc doc to parse
     * @return parsed account object
     */
    @SuppressWarnings("unchecked")
    public static Account fromDoc(Document doc) {
        Account a = new Account();
        a.username = (String) doc.get("username");
        a.email = (String) doc.get("email");
        a.passwordHash = (String) doc.get("password_hash");
        a.token = (String) doc.get("token");
        a.firstName = (String) doc.get("first_name");
        a.lastName = (String) doc.get("last_name");
        a.bio = (String) doc.get("biography");
        a.profilePictureLink = (String) doc.get("profile_picture_link");
        a.permissionID = (int) doc.get("permission_id");
        a.badgeIDs = (List<Integer>) doc.get("badge_ids");
        a.following = (List<String>) doc.get("following");
        a.signupDate = (Date) doc.get("signup_date");
        // "Unchecked cast" problem when casting to List
        // https://stackoverflow.com/questions/1490134/java-type-safety-unchecked-cast

        return a;
    }
}

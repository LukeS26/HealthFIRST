package LukeS26.github.io.dataschema;

import java.util.List;

import org.bson.Document;

/**
 * Used for interaction with MongoDB
 */
public class Profile extends DataSchema {
    // TODO: These shouldn't be public, write getters/setters
    public String username;
    public String email;
    public String passwordHash;

    public String firstName;
    public String lastName;

    public String profilePictureLink;
    public String bio; // Can be null

    public int permissionID;
    public List<Integer> badgeIDs; // Can be null/empty

    /**
     * Convert the profile object to a org.bson.Document
     * 
     * @param includePassword whether to include the password salt/hash in the
     *                        profile (true when sending the profile to the
     *                        frontend, false when working when profiles in the
     *                        backend)
     * @return profile formatted into a Document
     */
    public Document toDoc(boolean includePassword) {
        Document userDoc = new Document("username", username).append("first_name", firstName)
                .append("last_name", lastName).append("email", email).append("biography", bio)
                .append("profile_picture_link", profilePictureLink);

        if (includePassword) {
            userDoc.append("password_hash", passwordHash);
        }

        userDoc.append("permission_id", permissionID).append("badge_ids", badgeIDs);

        return userDoc;
    }

    /**
     * Converts profile to doc, assuming you want to show password
     * 
     * @return profile formatted into a Document
     */
    @Override
    public Document toDoc() {
        Document userDoc = new Document("username", username).append("first_name", firstName)
                .append("last_name", lastName).append("email", email).append("biography", bio)
                .append("profile_picture_link", profilePictureLink).append("password_hash", passwordHash)
                .append("permission_id", permissionID).append("badge_ids", badgeIDs);

        return userDoc;
    }

    /**
     * Parse a document into a profile
     * 
     * @param doc doc to parse
     * @return parsed Profile object
     */
    @SuppressWarnings("unchecked")
    public static Profile fromDoc(Document doc) {
        Profile p = new Profile();
        p.username = (String) doc.get("username");
        p.email = (String) doc.get("email");
        p.passwordHash = (String) doc.get("password_hash");
        p.firstName = (String) doc.get("first_name");
        p.lastName = (String) doc.get("last_name");
        p.profilePictureLink = (String) doc.get("profile_picture_link");
        p.permissionID = (int) doc.get("permission_id");
        p.badgeIDs = (List<Integer>) doc.get("badge_ids");
        // "Unchecked cast" problem when casting to List
        // https://stackoverflow.com/questions/1490134/java-type-safety-unchecked-cast

        return p;
    }
}

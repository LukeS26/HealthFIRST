package LukeS26.github.io.dataschema;

import java.util.UUID;

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import LukeS26.github.io.Settings;

public class Token extends DataSchema {
    public String tokenStr;
    public String username;

    /**
     * Generate a blank token (for working with tokens from database)
     */
    public Token() {

    }

    public Token(String username) {
        this.username = username;

        String salt = BCrypt.gensalt(Settings.BCRYPT_LOG_ROUNDS);
        this.tokenStr = BCrypt.hashpw(UUID.randomUUID().toString(), salt);
    }

    @Override
    public Document toDoc() {
        Document tokenDoc = new Document("token", tokenStr).append("username", username);
        return tokenDoc;
    }

    public static Token fromDoc(Document doc) {
        Token t = new Token();
        t.tokenStr = (String) doc.get("token");
        t.username = (String) doc.get("username");

        return t;
    }
}
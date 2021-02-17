package LukeS26.github.io.dataschema;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import LukeS26.github.io.Settings;

public class Token extends DataSchema {
    public String tokenStr;
    public String username;
    public String expiration;

    /**
     * Generate a blank token (for working with tokens from database)
     */
    public Token() {

    }

    public Token(String username, boolean expire) {
        this.username = username;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        DateTimeFormatter dtf = DateTimeFormatter.RFC_1123_DATE_TIME;
        if (expire) {
            ZonedDateTime expirationDate = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1);
            expiration = dtf.format(expirationDate);
            System.out.println("Expiration: " + expiration);

        } else {
            ZonedDateTime expirationDate = ZonedDateTime.now(ZoneId.of("UTC")).plusYears(100);
            expiration = dtf.format(expirationDate);
            System.out.println("Expiration: " + expiration);
        }

        String salt = BCrypt.gensalt(Settings.BCRYPT_LOG_ROUNDS);
        this.tokenStr = BCrypt.hashpw(UUID.randomUUID().toString(), salt);

        System.out.println("Token: " + tokenStr + " expiration: " + (expiration != null ? this.expiration.toString() : "null"));
    }

    @Override
    public Document toDoc() {
        Document tokenDoc = new Document("token", tokenStr).append("username", username).append("expiration_date",
                expiration);
        return tokenDoc;
    }

    public static Token fromDoc(Document doc) {
        Token t = new Token();
        t.tokenStr = (String) doc.get("token");
        t.username = (String) doc.get("username");
        t.expiration = (String) doc.get("expiration_date");

        return t;
    }
}

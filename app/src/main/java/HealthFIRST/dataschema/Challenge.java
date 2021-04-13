package HealthFIRST.dataschema;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Challenge extends DataSchema {
    public ObjectId id;

    public int challengeId; // challengeId = badgeId to give to user

    public String title;
    public String body;

    public Date startDate;
    public Date endDate;

    public Document toDoc() {
        return new Document("challenge_id", challengeId).append("title", title).append("body", body).append("start_date", startDate).append("end_date", endDate);
    }

    public static Challenge fromDoc(Document doc) {
        Challenge c = new Challenge();
        c.id = (ObjectId) doc.get("_id");
        c.challengeId = (int) doc.get("challenge_id");
        c.title = (String) doc.get("title");
        c.body = (String) doc.get("body");
        c.startDate = (Date) doc.get("start_date");
        c.endDate = (Date) doc.get("end_date");

        return c;
    }
}

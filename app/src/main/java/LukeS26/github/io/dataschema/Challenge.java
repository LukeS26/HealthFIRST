package LukeS26.github.io.dataschema;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Challenge extends DataSchema {
    public ObjectId id;
    
    public String title;
    public String body;

    public Date startDate;
    public Date endDate;

    @Override
    public Document toDoc() {
        Document challengeDoc = new Document("title", title).append("body", body).append("start_date", startDate).append("end_date", endDate);
        return challengeDoc;
    }

    public static Challenge fromDoc(Document doc) {
        Challenge c = new Challenge();
        c.id = (ObjectId) doc.get("_id");
        c.title = (String) doc.get("title");
        c.body = (String) doc.get("body");
        c.startDate = (Date) doc.get("start_date");
        c.endDate = (Date) doc.get("end_date");

        return c;
    }
}

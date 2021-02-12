package LukeS26.github.io.dataschema;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Comment extends DataSchema {
    /**
     * The parentID should be the post it is replying to or the other comment it is
     * replying to
     */
    public ObjectId id;
    public ObjectId parentId; // This will be the post ID if it is a comment on a post, and a comment ID if a reply on a comment
    public String author;
    public String body;

    /**
     * Same as Post.toDoc, this can't include the ID since this is used for new
     * posts that aren't in the database as well as posts already in the database
     * 
     * @return Document containing Comment information
     */
    @Override
    public Document toDoc() {
        Document commentDoc = new Document("parent_id", parentId).append("author", author).append("body", body);
        return commentDoc;
    }

    public static Comment fromDoc(Document doc) {
        Comment c = new Comment();
        c.id = (ObjectId) doc.get("_id");
        c.parentId = (ObjectId) doc.get("parent_id");
        c.author = (String) doc.get("author");
        c.body = (String) doc.get("body");

        return c;
    }
}

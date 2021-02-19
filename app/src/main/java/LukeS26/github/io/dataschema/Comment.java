package LukeS26.github.io.dataschema;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Comment extends DataSchema {
    /**
     * The parentID should be the post it is replying to or the other comment it is
     * replying to
     */
    public ObjectId id;
    public ObjectId postId;
    public ObjectId replyToId; // Initialized if it is a reply on a comment, null if a comment on a post
    public String author;
    public String body;
    // TODO: Keep post ID and a replyTo ID for the comment it is replying to, this can be null if it is a comment on a post

    /**
     * Same as Post.toDoc, this can't include the ID since this is used for new
     * posts that aren't in the database as well as posts already in the database
     * 
     * @return Document containing Comment information
     */
    @Override
    public Document toDoc() {
        Document commentDoc = new Document("post_id", postId).append("reply_to_id", replyToId).append("author", author).append("body", body);
        return commentDoc;
    }

    public static Comment fromDoc(Document doc) {
        Comment c = new Comment();
        c.id = (ObjectId) doc.get("_id");
        c.postId = (ObjectId) doc.get("post_id");
        c.replyToId = (ObjectId) doc.get("reply_to_id");
        c.author = (String) doc.get("author");
        c.body = (String) doc.get("body");

        return c;
    }
}

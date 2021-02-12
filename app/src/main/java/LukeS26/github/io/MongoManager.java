package LukeS26.github.io;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.types.ObjectId;

import LukeS26.github.io.dataschema.Comment;
import LukeS26.github.io.dataschema.Post;
import LukeS26.github.io.dataschema.Profile;

public class MongoManager {
    private MongoClient mongo;
    private MongoDatabase db;

    public MongoManager() {
        mongo = new MongoClient(Settings.MONGO_URI, Settings.MONGO_PORT);
        db = mongo.getDatabase(Settings.MONGO_DATABASE_NAME);
    }

    //#region Comments
    public void writeComment(Comment comment) {
        System.out.println("Writing comment...");
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        Document commentDoc = comment.toDoc();
        commentCollection.insertOne(commentDoc);
    }

    public Comment getComment(String commentID) {
        System.out.println("Getting comment ID: " + commentID);
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        Document commentDoc = commentCollection.find(Filters.eq("_id", new ObjectId(commentID))).first();

        if (commentDoc != null) {
            return Comment.fromDoc(commentDoc);
        }

        return null;
    }

    public FindIterable<Document> getReplies(String parentID) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        FindIterable<Document> docList = commentCollection.find(Filters.eq("parent_id", new ObjectId(parentID)));
        return docList;
    }
    //#endregion

    //#region Posts
    public void writePost(Post post) {
        System.out.println("Writing post...");
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        Document postDoc = post.toDoc();
        postCollection.insertOne(postDoc);
    }

    public Post getPost(String postID) {
        System.out.println("Getting post ID: " + postID);
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        Document postDoc = postCollection.find(Filters.eq("_id", new ObjectId(postID))).first();

        if (postDoc != null) {
            return Post.fromDoc(postDoc);
        }

        return null;
    }
    //#endregion

    //#region Profiles
    /**
     * Write a profile to the database without checking for duplicates
     * 
     * @param profile the profile to write
     */
    public void writeProfile(Profile profile) {
        System.out.println("Writing profile...");
        MongoCollection<Document> profileCollection = db.getCollection(Settings.PROFILE_COLLECTION_NAME);
        Document profileDoc = profile.toDoc(true);
        profileCollection.insertOne(profileDoc);
    }

    /**
     * Gets the profile from the database with the given username. Can be null.
     * 
     * @param username the username to check for
     * @return a profile object for that user, can be null if no profile was found
     */
    public Profile getProfile(String username) {
        MongoCollection<Document> profileCollection = db.getCollection(Settings.PROFILE_COLLECTION_NAME);
        Document profileDoc = profileCollection.find(Filters.eq("username", username)).first();
        return (profileDoc != null ? Profile.fromDoc(profileDoc) : null);
    }
    //#endregion
}
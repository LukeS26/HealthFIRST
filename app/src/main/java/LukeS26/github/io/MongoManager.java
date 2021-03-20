package LukeS26.github.io;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import org.bson.Document;
import org.bson.types.ObjectId;

import LukeS26.github.io.dataschema.Account;
import LukeS26.github.io.dataschema.Comment;
import LukeS26.github.io.dataschema.Post;

public class MongoManager {
    private static MongoManager instance;
    private MongoClient mongo;
    private MongoDatabase db;

    public static MongoManager getInstance() {
        if (instance == null) {
            instance = new MongoManager();
        }

        return instance;
    }

    private MongoManager() {
        mongo = new MongoClient(new MongoClientURI(Settings.MONGO_URI));
        db = mongo.getDatabase(Settings.MONGO_DATABASE_NAME);
    }

    // #region Comments
    /**
     * Get all comments and replies to comments for the given post
     * 
     * @param postID
     * @return
     */
    public FindIterable<Document> findAllComments(String postID) {
        MongoCollection<Document> commentsCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        FindIterable<Document> postComments = commentsCollection.find(Filters.eq("post_id", new ObjectId(postID)));
        return postComments;
    }

    public void writeComment(Comment comment) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        Document commentDoc = comment.toDoc();
        commentCollection.insertOne(commentDoc);
    }

    public Document findComment(String commentID) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        try {
            Document commentDoc = commentCollection.find(Filters.eq("_id", new ObjectId(commentID))).first();
            if (commentDoc != null) {
                return commentDoc;
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }
    // TODO: Allow editing of comments (might want to add a boolean for isEdited to
    // show if it was edited like Discord)
    // #endregion

    // #region Posts
    public void deletePost(Document post) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        postCollection.findOneAndDelete(Filters.eq("_id", post.get("_id")));
    }

    public void writePost(Post post) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        Document postDoc = post.toDoc();
        postCollection.insertOne(postDoc);
    }

    public Document findPost(String postID) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        try {
            Document postDoc = postCollection.find(Filters.eq("_id", new ObjectId(postID))).first();
            if (postDoc != null) {
                return postDoc;
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }

    public FindIterable<Document> getFeed(int pageNumber) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        try {
            // TODO: For some reason this is making 0 and 1 equal
            FindIterable<Document> postDocs = postCollection.find().sort(Sorts.descending("date"))
                    .skip(Settings.POSTS_PER_PAGE * pageNumber).limit(Settings.POSTS_PER_PAGE);
            if (postDocs != null) {
                return postDocs;
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }
    // #endregion

    // #region Accounts
    /**
     * Write a account to the database without checking for duplicates
     * 
     * @param account the account to write
     */
    public void writeAccount(Account account) {
        System.out.println("Writing account...");
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        Document accountDoc = account.toDoc(true);
        accountCollection.insertOne(accountDoc);
    }

    /**
     * Gets the account document from the database with the given username.
     * 
     * @param username the username to find a doc for
     * @return an org.bson.Document for the given username if found, null if not
     *         found
     */
    public Document findAccount(String username) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        try {
            Document accountDoc = accountCollection.find(Filters.eq("username", username)).first();
            if (accountDoc != null) {
                return accountDoc;
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public Document findAccountByToken(String token) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        try {
            Document accountDoc = accountCollection.find(Filters.eq("token", token)).first();
            if (accountDoc != null) {
                return accountDoc;
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public void updateAccount(String username, Document update) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        Document updateDoc = new Document("$set", update);
        try {
            accountCollection.findOneAndUpdate(Filters.eq("username", username), updateDoc);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    // #endregion
}
package LukeS26.github.io;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.types.ObjectId;

import LukeS26.github.io.dataschema.Account;
import LukeS26.github.io.dataschema.Comment;
import LukeS26.github.io.dataschema.Post;
import LukeS26.github.io.dataschema.Token;

public class MongoManager {
    private MongoClient mongo;
    private MongoDatabase db;

    public MongoManager() {
        mongo = new MongoClient(new MongoClientURI(Settings.MONGO_URI));
        db = mongo.getDatabase(Settings.MONGO_DATABASE_NAME);
    }

    // #region Authentication
    public void writeToken(Token token) {
        MongoCollection<Document> tokenCollection = db.getCollection(Settings.TOKENS_COLLECTION_NAME);
        Document tokenDoc = token.toDoc();
        tokenCollection.insertOne(tokenDoc);
    }

    /**
     * Find a token in the database based on the hashed token (Used in requests
     * where token is used in the authorization header)
     * 
     * @param token hashed token
     * @return document with token if found, null if not found
     */
    public Document findToken(String token) {
        MongoCollection<Document> tokenCollection = db.getCollection(Settings.TOKENS_COLLECTION_NAME);
        try {
            Document tokenDoc = tokenCollection.find(Filters.eq("token", token)).first();
            if (tokenDoc != null) {
                return tokenDoc;
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }
    // #endregion

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
    // TODO: Allow editing of comments (might want to add a boolean for isEdited to show if it was edited like Discord)
    // #endregion

    // #region Posts
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

    public void updateAccount(String username, Document update) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        try {
            Document accountDoc = accountCollection.find(Filters.eq("username", username)).first();
            if (accountDoc != null) {
                accountCollection.findOneAndUpdate(Filters.eq("username", username), update);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    // #endregion
}
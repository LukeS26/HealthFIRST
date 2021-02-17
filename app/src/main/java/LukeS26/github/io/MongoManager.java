package LukeS26.github.io;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
     * @return document with token if found, null if not found (expired tokens are
     *         deleted)
     */
    public Token findToken(String token) {
        cleanTokens();
        MongoCollection<Document> tokenCollection = db.getCollection(Settings.TOKENS_COLLECTION_NAME);
        try {
            FindIterable<Document> tokenDocs = tokenCollection.find();
            for (Document tokenDoc : tokenDocs) {
                if (token.equals((String) tokenDoc.get("token"))) {
                    return Token.fromDoc(tokenDoc);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public Token getToken(String username) {
        cleanTokens();
        MongoCollection<Document> tokenCollection = db.getCollection(Settings.TOKENS_COLLECTION_NAME);
        try {
            Document tokenDoc = tokenCollection.find(Filters.eq("username", username)).first();
            if (tokenDoc != null) {
                return Token.fromDoc(tokenDoc);
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }

    /**
     * Remove a token from the database
     * 
     * @param id ObjectId of token to remove
     */
    public void deleteToken(ObjectId id) {
        MongoCollection<Document> tokenCollection = db.getCollection(Settings.TOKENS_COLLECTION_NAME);
        tokenCollection.findOneAndDelete(Filters.eq("_id", id));
    }

    /**
     * Removes expired tokens
     */
    public void cleanTokens() {
        MongoCollection<Document> tokenCollection = db.getCollection(Settings.TOKENS_COLLECTION_NAME);
        FindIterable<Document> tokenDocs = tokenCollection.find();

        for (Document doc : tokenDocs) {
            String expirationString = (String) doc.get("expiration_date");
            if (expirationString != null) {
                DateTimeFormatter dtf = DateTimeFormatter.RFC_1123_DATE_TIME;
                ZonedDateTime parsedExpiration = ZonedDateTime.parse(expirationString, dtf);

                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

                if (parsedExpiration.isBefore(now)) {
                    System.out.println("Deleting expired token for user: " + (String) doc.get("username"));
                    tokenCollection.deleteOne(Filters.eq("_id", (ObjectId) doc.get("_id")));
                }
            }
        }
    }
    // #endregion

    // #region Comments

    /**
     * Get all comments and replies to comments for the given post
     * 
     * @param postID
     * @return
     */
    public Document getAllComments(String postID) {
        MongoCollection<Document> commentsCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        FindIterable<Document> postComments = commentsCollection.find(Filters.eq("parent_id", new ObjectId(postID)));

        List<Document> allComments = new ArrayList<>();
        List<Document> toProcess = new ArrayList<>();
        for (Document doc : postComments) {
            toProcess.add(doc);
            allComments.add(doc);
        }

        while (toProcess.size() > 0) {
            Document curDoc = toProcess.get(toProcess.size() - 1);
            toProcess.remove(toProcess.size() - 1);
            FindIterable<Document> children = commentsCollection
                    .find(Filters.eq("parent_id", (ObjectId) curDoc.get("_id")));
            MongoCursor<Document> childrenCursor = children.cursor();
            while (childrenCursor.hasNext()) {
                Document child = childrenCursor.next();
                allComments.add(child);
                toProcess.add(child);
            }
        }

        Document returnDoc = new Document("comments", allComments);
        return returnDoc;
    }

    public void writeComment(Comment comment) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        Document commentDoc = comment.toDoc();
        commentCollection.insertOne(commentDoc);
    }

    public Comment getComment(String commentID) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        try {
            Document commentDoc = commentCollection.find(Filters.eq("_id", new ObjectId(commentID))).first();
            if (commentDoc != null) {
                return Comment.fromDoc(commentDoc);
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }

    public FindIterable<Document> getReplies(String parentID) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        try {
            FindIterable<Document> docList = commentCollection.find(Filters.eq("parent_id", new ObjectId(parentID)));
            if (docList.cursor().hasNext()) {
                return docList;
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }
    // #endregion

    // #region Posts
    public void writePost(Post post) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        Document postDoc = post.toDoc();
        postCollection.insertOne(postDoc);
    }

    public Post getPost(String postID) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        try {
            Document postDoc = postCollection.find(Filters.eq("_id", new ObjectId(postID))).first();
            if (postDoc != null) {
                return Post.fromDoc(postDoc);
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
     * Gets the account from the database with the given username. Can be null.
     * 
     * @param username the username to check for
     * @return a account object for that user, can be null if no account was found
     */
    public Account getAccount(String username) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        try {
            Document accountDoc = accountCollection.find(Filters.eq("username", username)).first();
            if (accountDoc != null) {
                return Account.fromDoc(accountDoc);
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }
    // #endregion
}
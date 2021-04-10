package LukeS26.github.io;

import LukeS26.github.io.dataschema.Account;
import LukeS26.github.io.dataschema.Challenge;
import LukeS26.github.io.dataschema.Comment;
import LukeS26.github.io.dataschema.Post;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class MongoManager {
    private static MongoManager instance;
    private final MongoDatabase db;

    public static MongoManager getInstance() {
        if (instance == null) {
            instance = new MongoManager();
        }

        return instance;
    }

    private MongoManager() {
        MongoClient mongo = new MongoClient(new MongoClientURI(Settings.MONGO_URI));
        db = mongo.getDatabase(Settings.MONGO_DATABASE_NAME);
    }

    public void editComment(ObjectId originalID, Document bsonUpdate) {
        bsonUpdate.put("edited", true);
        MongoCollection<Document> commentsCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        commentsCollection.findOneAndUpdate(Filters.eq("_id", originalID), new Document("$set", bsonUpdate));
    }

    public void deleteComment(String commentID) {
        MongoCollection<Document> commentsCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        commentsCollection.updateOne(Filters.eq("_id", commentID),
                new Document("$set", new Document("author", "[Removed]").append("body", "[Removed]")));
    }

    // #region Comments
    /**
     * Get all comments and replies to comments for the given post
     * 
     * @param postID ID of comment to find
     * @return FindIterable of located documents
     */
    public FindIterable<Document> findAllComments(String postID) {
        MongoCollection<Document> commentsCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        return commentsCollection.find(Filters.eq("post_id", new ObjectId(postID)));
    }

    public void writeComment(Comment comment) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        Document commentDoc = comment.toDoc();
        commentCollection.insertOne(commentDoc);
    }

    public void writeCommentDoc(Document commentDoc) {
        MongoCollection<Document> commentCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
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
    // #endregion

    // #region Posts
    public void editPost(ObjectId originalID, Document bsonUpdate) {
        bsonUpdate.put("edited", true);
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        postCollection.findOneAndUpdate(Filters.eq("_id", originalID), new Document("$set", bsonUpdate));
    }

    public void deletePost(Document post) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        postCollection.findOneAndUpdate(Filters.eq("_id", post.get("_id")), new Document("$set",
                new Document("title", "[Removed]").append("author", "[Removed]").append("body", "[Removed]")));
    }

    public void writePost(Post post) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        Document postDoc = post.toDoc();
        postCollection.insertOne(postDoc);
    }

    public void writePostDoc(Document doc) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        postCollection.insertOne(doc);
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

    public FindIterable<Document> findPostsForUser(String username) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        try {
            FindIterable<Document> posts = postCollection.find(Filters.eq("username", username));
            if (posts != null) {
                return posts;
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return null;
    }

    public FindIterable<Document> getChallengeFeed(int pageNumber) {
        MongoCollection<Document> challengesCollection = db.getCollection(Settings.CHALLENGES_COLLECTION_NAME);
        try {
            FindIterable<Document> challengeDocs = challengesCollection.find().sort(Sorts.descending("challenge_id"))
                    .skip(Settings.CHALLENGES_PER_PAGE * pageNumber).limit(Settings.CHALLENGES_PER_PAGE);
            if (challengeDocs != null) {
                return challengeDocs;
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public FindIterable<Document> getFeed(int pageNumber) {
        MongoCollection<Document> postCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        try {
            FindIterable<Document> postDocs = postCollection.find(Filters.not(Filters.eq("author", "[Removed]"))).sort(Sorts.descending("date"))
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

    // #region Challenges
    public Document findChallengeById(int challengeId) {
        MongoCollection<Document> challengesCollection = db.getCollection(Settings.CHALLENGES_COLLECTION_NAME);
        try {
            return challengesCollection.find(Filters.eq("challenge_id", challengeId)).first();
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public Document findCurrentChallenge() {
        MongoCollection<Document> challengesCollection = db.getCollection(Settings.CHALLENGES_COLLECTION_NAME);
        return challengesCollection.find().sort(Sorts.descending("date")).first();
    }

    @SuppressWarnings("unchecked")
    public void completeChallenge(int challengeId, Account account) {
        account.badgeIDs.add(challengeId);

        Document updateDoc = new Document();
        updateDoc.put("badge_ids", account.badgeIDs);

        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        Document accountDoc = accountCollection.find(Filters.eq("username", account.username)).first();
        if (accountDoc == null) {
            return;
        }

        if (((List<Integer>) accountDoc.get("badge_ids")).contains(challengeId)) {
            return;
        }


        MongoCollection<Document> challengeCollection = db.getCollection(Settings.CHALLENGES_COLLECTION_NAME);
        Document challengeDoc = challengeCollection.find(Filters.eq("challenge_id", challengeId)).first();
        if (challengeDoc == null) {
            return;
        }

        if (((Date) challengeDoc.get("end_date")).before(new Date())) {
            return;
        }

        accountCollection.updateOne(Filters.eq("username", account.username), new Document("$set", updateDoc));
    }

    public void writeChallenge(Challenge challenge) {
        MongoCollection<Document> challengesCollection = db.getCollection(Settings.CHALLENGES_COLLECTION_NAME);
        Document challengeDoc = challenge.toDoc();
        challengesCollection.insertOne(challengeDoc);
    }
    // //#endregion

    // #region Accounts
    /**
     * Write a account to the database without checking for duplicates
     * 
     * @param account the account to write
     */
    public void writeAccount(Account account) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        Document accountDoc = account.toDoc(true);
        accountCollection.insertOne(accountDoc);
    }

    public void deleteAccount(String username) {
        MongoCollection<Document> postsCollection = db.getCollection(Settings.POSTS_COLLECTION_NAME);
        MongoCollection<Document> commentsCollection = db.getCollection(Settings.COMMENTS_COLLECTION_NAME);
        MongoCollection<Document> accountsCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);

        postsCollection.updateMany(Filters.eq("author", username), new Document("$set",
                new Document("title", "[Removed]").append("author", "[Removed]").append("body", "[Removed]")));
        commentsCollection.updateMany(Filters.eq("author", username),
                new Document("$set", new Document("author", "[Removed]").append("body", "[Removed]")));
        accountsCollection.deleteOne(Filters.eq("username", username));
    }

    /**
     * Gets the account document from the database with the given username.
     * 
     * @param username the username to find a doc for
     * @return an org.bson.Document for the given username if found, null if not
     *         found
     */
    public Document findAccount(String username, boolean ignoreCase) {
        MongoCollection<Document> accountCollection = db.getCollection(Settings.ACCOUNTS_COLLECTION_NAME);
        Document accountDoc = null;
        try {
            if (ignoreCase) {
                accountDoc = accountCollection.find(Filters.regex("username", Pattern.compile("^(?i)" + username + "(?-i)$"))).first();

            } else {
                accountDoc = accountCollection.find(Filters.eq("username", username)).first();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return accountDoc;
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
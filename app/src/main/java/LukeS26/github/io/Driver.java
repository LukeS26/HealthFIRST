package LukeS26.github.io;

public class Driver {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();

        server.start();

        
        //System.out.println(server.mongoManager.getAllComments("6026d329da97123844466de5").toJson());
    }
    
    // server.testLogin("JohnSmith72", "testhash12345");
    
    // Create account
    // curl -d "{'username': 'JohnSmith72', 'first_name': 'John', 'last_name': 'Smith', 'email': 'johnsmith@gmail.com', 'password_hash': 'testhash12345'}" 127.0.0.1/api/account/signup
    
    // Create post
    // curl -d "{'author': 'JohnSmith72', 'title': 'Example Title', 'body': 'Example body text'}" 127.0.0.1/api/posts

    // Get post
    // curl 127.0.0.1/api/posts/6026a7d425910c7e75ee00d3

    // Get comment
    // curl 127.0.0.1/api/comments/6026a80425910c7e75ee00d4

    // Create comment
    // curl -d "{'parent_id': {'$oid': '6026a7d425910c7e75ee00d3'}, 'author': 'JohnSmith72', 'body': 'Example body text'}" 127.0.0.1/api/comments
    
    // Create reply
    // curl -d "{'parent_id': {'$oid': '6026a80425910c7e75ee00d4'}, 'author': 'JohnSmith72', 'body': 'Example body text reply'}" 127.0.0.1/api/comments
    
    // Get replies
    // curl 127.0.0.1/api/replies/6026a7d425910c7e75ee00d3
}

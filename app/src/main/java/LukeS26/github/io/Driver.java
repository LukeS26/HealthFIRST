package LukeS26.github.io;

public class Driver {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();

        server.start();

        // server.testLogin("JohnSmith72", "testhash12345");

        // Create account
        // curl -d "{'username': 'JohnSmith72', 'first_name': 'John', 'last_name': 'Smith', 'email': 'johnsmith@gmail.com', 'password_hash': 'testhash12345'}" 127.0.0.1/api/account/signup
        
        // Create post
        // curl -d "{'author': 'JohnSmith72', 'title': 'Example Title', 'body': 'Example body text'}" 127.0.0.1/api/posts

        // Get post
        // curl 127.0.0.1/api/posts/602691a372c4993074254496

        // Get comment
        // curl 127.0.0.1/api/comments/6026a200bab2c15b086cecdb

        // Create comment
        // curl -d "{'parent_id': {'$oid': '602691a372c4993074254496'}, 'author': 'JohnSmith72', 'body': 'Example body text'}" 127.0.0.1/api/comments
    }
}

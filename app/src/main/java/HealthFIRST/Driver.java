package HealthFIRST;

public class Driver {
    public static void main(String[] args) {
        HttpServer server = HttpServer.getInstance();

        server.start();
    }
}

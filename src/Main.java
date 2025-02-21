import com.sun.net.httpserver.HttpServer;
import server.MyServer;

import java.io.IOException;

import static handlers.MyHandler.initRoutes;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = MyServer.makeServer();
            initRoutes(server);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
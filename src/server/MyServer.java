package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyServer {

    public static HttpServer makeServer() throws IOException {
        String host = "localhost";
        InetSocketAddress address = new InetSocketAddress(host, 9889);

        System.out.printf(
                "Started server on %s:%d",
                address.getHostName(),
                address.getPort()
        );

        HttpServer server = HttpServer.create(address, 50);
        System.out.println("    successfully started");
        return server;
    }
}

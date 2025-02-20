import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = makeServer();
            initRoutes(server);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpServer makeServer() throws IOException {
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

    private static void initRoutes(HttpServer server) {
        server.createContext("/", exchange -> handleRequestHtml(exchange));
        server.createContext("/apps/", exchange -> handleRequest(exchange));
        server.createContext("/apps/profile", exchange -> handleRequest(exchange));
    }

    private static void handleRequestHtml(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String filePath = "src" + uri.getPath();
            Path path = Paths.get(filePath);
            String mimeType = getMimeType(path);

            if (uri.getPath().contains(".html")) {
                // Указываем относительный путь к файлу

                if (Files.exists(path)) {
                    String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", mimeType + "; charset=utf-8");
                    exchange.sendResponseHeaders(200, content.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(content.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    String response = "File not found";
                    exchange.sendResponseHeaders(404, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else {
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                int responseCode = 200;
                int length = 0;
                exchange.sendResponseHeaders(responseCode, length);

                try (PrintWriter writer = getWriterFrom(exchange)) {
                    String pathString = exchange.getHttpContext().getPath();
                    write(writer, "HTTP method", method);
                    write(writer, "Request", uri.toString());
                    write(writer, "Handler", pathString);
                    writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                    writeData(writer, exchange);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void handleRequest(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");

            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode, length);

            try (PrintWriter writer = getWriterFrom(exchange)) {
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();

                String path = exchange.getHttpContext().getPath();

                write(writer, "HTTP method", method);
                write(writer, "Request", uri.toString());
                write(writer, "Handler", path);
                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                writeData(writer, exchange);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendHtmlFile(HttpExchange exchange, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, content.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            String response = "File not found";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;
        return new PrintWriter(outputStream, false, charset);
    }

    private static void write(Writer writer, String msg, String method) {
        String body = String.format("%s: %s%n%n", msg, method);
        try {
            writer.write(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeaders(Writer writer, String type, Headers headers) {
        write(writer, type, "");
        headers.forEach((k, v) -> write(writer, "\t" + k, v.toString()));
    }

    private static BufferedReader getReader(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
        return new BufferedReader(inputStreamReader);
    }

    private static void writeData(Writer writer, HttpExchange exchange) {
        try (BufferedReader reader = getReader(exchange)) {
            if (!reader.ready()) return;

            write(writer, "Data", "");
            reader.lines().forEach(line -> write(writer, "\t", line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getMimeType(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString();
        if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".html")) {
            return "text/html";
        } else {
            return "text/plain";
        }
    }



// пример



    private static void initRoutes1(HttpServer server) {
        server.createContext("/", new MyHandler());
        server.createContext("/apps/", new MyHandler());
        server.createContext("/apps/profile", new MyHandler());
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            Path filePath = Paths.get("path/to/your/static/files" + requestPath);

            if (Files.exists(filePath)) {
                String mimeType = getMimeType(filePath);
                exchange.getResponseHeaders().add("Content-Type", mimeType);
                exchange.sendResponseHeaders(200, Files.size(filePath));
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(filePath, os);
                }
            } else {
                String response = "File not found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private String getMimeType(Path filePath) throws IOException {
            String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(".css")) {
                return "text/css";
            } else if (fileName.endsWith(".html")) {
                return "text/html";
            } else {
                return "text/plain";
            }
        }
    }
}
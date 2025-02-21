package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyHandler {

    public static void initRoutes(HttpServer server) {
        server.createContext("/", exchange -> handleRequest(exchange));
        server.createContext("/apps", exchange -> handleRequestApps(exchange));
        server.createContext("/apps/profile", exchange -> handleRequestProfile(exchange));
    }

    private static void handleRequestFile(HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            if (uri.getPath().equalsIgnoreCase("/")) {
                handleRequest(exchange);
                return;
            }
            String filePath = "src/" + uri.getPath();
            Path path = Paths.get(filePath);
            String mimeType = getMimeType(filePath);

            if (Files.exists(path)) {
                byte[] content = Files.readAllBytes(path);
                exchange.getResponseHeaders().add("Content-Type", mimeType + "; charset=utf-8");
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } else {
                String response = "404\nFile not found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequestProfile(HttpExchange exchange) {
        try {
            if (isFile(exchange)) {
                handleRequestFile(exchange);
            } else {
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequestApps(HttpExchange exchange) {
        try {
            if (isFile(exchange)) {
                handleRequestFile(exchange);
            } else {
                exchange.getResponseHeaders().add("Content-Type", "application/xml; charset=utf-8");

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(HttpExchange exchange) {
            try {
                if (isFile(exchange)) {
                    handleRequestFile(exchange);
                } else {
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
                }
            } catch (IOException e) {
                e.printStackTrace();
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

    private static boolean isFile(HttpExchange exchange) {
        return Files.exists(Path.of("src/" + exchange.getRequestURI().getPath()));
    }

    private static String getMimeType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html; charset=utf-8";
        } else if (filePath.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "text/plain; charset=utf-8";
        }
    }
}

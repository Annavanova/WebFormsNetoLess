package netoTestWebForms;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormsServer {
    private final Map<String, Map<String, WebHandlers>> handlersMap = new ConcurrentHashMap<>();
    private final int SERVER_SOCKET;
    private static final int poolSizeThreads = 64;

    final ExecutorService threadPool = Executors.newFixedThreadPool(poolSizeThreads);

    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");

    public FormsServer(int SERVER_SOCKET) {
        this.SERVER_SOCKET = SERVER_SOCKET;
    }

    public void start() {

        try (final ServerSocket serverSocket = new ServerSocket(SERVER_SOCKET)) {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    Runnable hendlerSocketRunnable = new HendlerSocketRunnable(socket, validPaths, this);
                    threadPool.submit(hendlerSocketRunnable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void responseWithoutContent(BufferedOutputStream out, String responseCode, String responseStatus) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    void defaultHandler(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.startsWith("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }


    protected void addHandler(String method, String path, WebHandlers webHandlers) {
        if (!handlersMap.containsKey(method)) {
            handlersMap.put(method, new HashMap<>());
        }
        handlersMap.get(method).put(path, webHandlers);
    }

}

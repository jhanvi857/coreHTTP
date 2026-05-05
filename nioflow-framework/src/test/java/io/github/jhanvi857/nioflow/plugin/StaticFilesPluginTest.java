package io.github.jhanvi857.nioflow.plugin;

import io.github.jhanvi857.nioflow.NioFlowApp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class StaticFilesPluginTest {
    private static NioFlowApp app;
    private static int port;
    private static final HttpClient client = HttpClient.newHttpClient();

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setUp() throws Exception {
        // Create test files
        Files.writeString(tempDir.resolve("hello.txt"), "hello world");
        Files.writeString(tempDir.resolve("hello.html"), "<html><body>hello</body></html>");
        Files.writeString(tempDir.resolve("style.css"), "body { color: red; }");
        Files.createDirectory(tempDir.resolve("subdir"));
        Files.writeString(tempDir.resolve("subdir/inner.txt"), "inner");

        app = new NioFlowApp();
        app.register(new StaticFilesPlugin(tempDir.toString(), "/static"));
        
        new Thread(() -> app.listen(0)).start();
        
        long start = System.currentTimeMillis();
        while (app.getPort() == -1 && System.currentTimeMillis() - start < 5000) {
            Thread.sleep(100);
        }
        port = app.getPort();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.drainAndStop(500, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void serveExistingFile_returns200WithContent() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/hello.txt"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("hello world", response.body());
    }

    @Test
    public void serveNonexistentFile_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/missing.txt"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void pathTraversal_dotDot_rejected() throws Exception {
        // Attempt to go above the base directory
        // The StaticFileHandler.normalize() and requestedFile.startsWith(baseDir) should catch this.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/../../etc/passwd"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Depending on implementation, it might be 400 (Invalid path) or 404 (if normalized to something else)
        // StaticFileHandler line 51 says 400.
        assertTrue(response.statusCode() == 400 || response.statusCode() == 404);
        assertFalse(response.body().contains("root:"));
    }

    @Test
    public void pathTraversal_urlEncoded_rejected() throws Exception {
        // GET /static/%2F%2E%2E%2Fetc%2Fpasswd
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/%2F%2E%2E%2Fetc%2Fpasswd"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() == 400 || response.statusCode() == 404);
        assertFalse(response.body().contains("root:"));
    }

    @Test
    public void pathTraversal_nullByte_rejected() throws Exception {
        // HttpParser rejects null bytes in path, so this should return 400
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/hello.txt%00.jpg"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void directoryListing_forbidden() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Should return 404 or index.html if present, but never a directory listing.
        // StaticFileHandler line 71 says 404 for directories.
        assertTrue(response.statusCode() == 404 || response.statusCode() == 403);
        assertFalse(response.body().contains("Index of"));
        assertFalse(response.body().contains("hello.txt"));
    }

    @Test
    public void correctContentType_html() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/hello.html"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.headers().firstValue("Content-Type").get().contains("text/html"));
    }

    @Test
    public void correctContentType_css() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/style.css"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.headers().firstValue("Content-Type").get().contains("text/css"));
    }

    @Test
    public void correctContentType_json() throws Exception {
        Files.writeString(tempDir.resolve("data.json"), "{}");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/data.json"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(response.headers().firstValue("Content-Type").get().contains("application/json"));
    }

    @Test
    public void correctContentType_unknown_defaultsToOctetStream() throws Exception {
        Files.writeString(tempDir.resolve("binary.dat"), "0101");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/static/binary.dat"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("application/octet-stream", response.headers().firstValue("Content-Type").get());
    }
    @Test
    public void correctContentType_images() throws Exception {
        String[] extensions = {".png", ".jpg", ".gif", ".svg"};
        String[] mimes = {"image/png", "image/jpeg", "image/gif", "image/svg+xml"};
        
        for (int i = 0; i < extensions.length; i++) {
            String fileName = "test" + extensions[i];
            Files.writeString(tempDir.resolve(fileName), "binary");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/static/" + fileName))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(mimes[i], response.headers().firstValue("Content-Type").get(), "Failed for " + extensions[i]);
        }
    }
}

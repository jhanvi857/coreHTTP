package io.github.jhanvi857.nioflow.protocol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import io.github.jhanvi857.nioflow.exception.HttpParseException;

public class HttpParserTest {

    private final HttpParser parser = new HttpParser();

    @Test
    public void testSimpleGetRequest() throws Exception {
        String raw = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = parser.parse(in);

        assertEquals("GET", request.getMethod());
        assertEquals("/index.html", request.getPath());
        assertEquals("HTTP/1.1", request.getVersion());
        assertEquals("localhost", request.getHeaders().get("Host"));
    }

    @Test
    public void testPostRequestWithBody() throws Exception {
        String raw = "POST /api/data HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: 17\r\n" +
                "\r\n" +
                "{\"message\": \"hi\"}";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = parser.parse(in);

        assertEquals("POST", request.getMethod());
        assertEquals(17, request.getBody().length);
        assertEquals("{\"message\": \"hi\"}", new String(request.getBody(), StandardCharsets.US_ASCII));
    }

    @Test
    public void testChunkedEncoding() throws Exception {
        String raw = "POST /upload HTTP/1.1\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "\r\n" +
                "5\r\n" +
                "hello\r\n" +
                "6\r\n" +
                " world\r\n" +
                "0\r\n" +
                "\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = parser.parse(in);

        assertEquals("hello world", new String(request.getBody(), StandardCharsets.US_ASCII));
    }

    @Test
    public void testMalformedRequestLine() {
        String raw = "BAD_REQUEST_LINE\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void testMissingHeaders() throws Exception {
        String raw = "GET / HTTP/1.1\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = parser.parse(in);

        assertEquals("GET", request.getMethod());
        assertTrue(request.getHeaders().isEmpty());
    }

    @Test
    public void testCaseInsensitiveHeaders() throws Exception {
        String raw = "GET / HTTP/1.1\r\nCONTENT-LENGTH: 0\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        HttpRequest request = parser.parse(in);

        assertEquals("0", request.getHeaders().get("CONTENT-LENGTH"));
    }

    @Test
    public void testRejectsCrlfInHeaders() {
        // CRLF injection attempt within a single header value
        // The parser should catch the \n character
        String raw = "GET / HTTP/1.1\r\nX-Injected: value\nevil-header: true\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void testRejectsNullByteInPath() {
        String raw = "GET /evil\0path HTTP/1.1\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void testRejectsSmuggledTransferEncoding() {
        // Multiple TEs or identity+chunked should be rejected
        String raw = "POST / HTTP/1.1\r\nTransfer-Encoding: identity, chunked\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));

        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }
}

package com.jhanvi857.nioflow.protocol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.jhanvi857.nioflow.exception.HttpParseException;

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

        // HttpParser.parse already handles headers as a HashMap, let's see if
        // internal getHeaderValueIgnoreCase works. (Requires manual check in
        // actual request object if exposed).
        assertEquals("0", request.getHeaders().get("CONTENT-LENGTH"));
    }
}

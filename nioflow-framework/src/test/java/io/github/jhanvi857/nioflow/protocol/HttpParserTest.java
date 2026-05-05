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

    @Test
    public void parseRequest_duplicateCL_throwsOrReturnsNull() {
        String raw = "POST / HTTP/1.1\r\nContent-Length: 5\r\nContent-Length: 10\r\n\r\nhello";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parseRequest_teCLCollision_throwsOrReturnsNull() {
        String raw = "POST / HTTP/1.1\r\nTransfer-Encoding: chunked\r\nContent-Length: 5\r\n\r\n5\r\nhello\r\n0\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parseRequest_validRequest_parsedCorrectly() throws Exception {
        String raw = "GET /test HTTP/1.1\r\nHost: localhost\r\nX-Custom: value\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));
        HttpRequest request = parser.parse(in);
        assertEquals("GET", request.getMethod());
        assertEquals("/test", request.getPath());
        assertEquals("localhost", request.getHeaders().get("Host"));
        assertEquals("value", request.getHeaders().get("X-Custom"));
    }

    @Test
    public void parseRequest_crlfInHeader_rejected() {
        String raw = "GET / HTTP/1.1\r\nX-Bad: value\r\nInjected: true\r\n\r\n";
        // Wait, the parser handles CRLF by splitting lines.
        // The injection check is for CR or LF INSIDE the key or value.
        String raw2 = "GET / HTTP/1.1\r\nX-Bad: value\revil\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw2.getBytes(StandardCharsets.US_ASCII));
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parseRequest_nullByteInPath_rejected() {
        String raw = "GET /test\0path HTTP/1.1\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parseRequest_emptyMethod_rejected() {
        String raw = " / HTTP/1.1\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parseRequest_missingHttpVersion_rejected() {
        String raw = "GET /\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.US_ASCII));
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_chunkedBody_withExtensions_ignored() throws Exception {
        String req = "POST /chunked HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" +
                "5;key=val\r\nhello\r\n" +
                "0\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        HttpRequest request = parser.parse(in);
        assertEquals("hello", new String(request.getBody()));
    }

    @Test
    public void parse_chunkedBody_invalidSize_throws() {
        String req = "POST /chunked HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" +
                "XX\r\nhello\r\n" +
                "0\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(java.io.IOException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_headersTooLarge_throws() {
        String largeHeaders = "GET / HTTP/1.1\r\n" + "X-Header: value\r\n".repeat(600) + "\r\n";
        InputStream in = new ByteArrayInputStream(largeHeaders.getBytes());
        assertThrows(io.github.jhanvi857.nioflow.exception.RequestHeaderFieldsTooLargeException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_bodyTooLarge_throws() {
        String req = "POST /large HTTP/1.1\r\nContent-Length: 20000000\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(io.github.jhanvi857.nioflow.exception.PayloadTooLargeException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_invalidPath_rejected() {
        String req = "GET invalid-path HTTP/1.1\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_malformedHeader_missingColon_ignored() throws Exception {
        String req = "GET / HTTP/1.1\r\nMalformedHeaderLine\r\nHost: localhost\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        HttpRequest request = parser.parse(in);
        assertEquals("localhost", request.getHeaders().get("Host"));
        assertFalse(request.getHeaders().containsKey("MalformedHeaderLine"));
    }

    @Test
    public void parse_negativeContentLength_throws() {
        String req = "POST / HTTP/1.1\r\nContent-Length: -5\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_invalidContentLength_throws() {
        String req = "POST / HTTP/1.1\r\nContent-Length: abc\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_unsupportedTransferEncoding_throws() {
        String req = "POST / HTTP/1.1\r\nTransfer-Encoding: gzip\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_crlfInHeaderKey_throws() {
        String req = "GET / HTTP/1.1\r\nX-Bad\rKey: value\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_nullByteInHeaderKey_throws() {
        String req = "GET / HTTP/1.1\r\nX-Null\0Key: value\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_unexpectedEndOfStreamInBody_throws() {
        String req = "POST / HTTP/1.1\r\nContent-Length: 10\r\n\r\nhi";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(java.io.IOException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_negativeChunkSize_throws() {
        String req = "POST / HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n-5\r\nhello\r\n0\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(java.io.IOException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_missingCrlfAfterChunk_throws() {
        String req = "POST / HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n5\r\nhelloMISSING\r\n0\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(java.io.IOException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_lineTooLong_throws() {
        String req = "POST / HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" + "A".repeat(2000) + "\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(java.io.IOException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_invalidLineEnding_throws() {
        // \r followed by something other than \n
        String req = "POST / HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n5\rX\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(java.io.IOException.class, () -> parser.parse(in));
    }

    @Test
    public void parse_nullByteInHeadersBlock_throws() {
        String req = "GET / HTTP/1.1\r\nHost: local\0host\r\n\r\n";
        InputStream in = new ByteArrayInputStream(req.getBytes());
        assertThrows(HttpParseException.class, () -> parser.parse(in));
    }
}

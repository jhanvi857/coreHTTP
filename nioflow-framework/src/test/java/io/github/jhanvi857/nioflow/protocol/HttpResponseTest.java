package io.github.jhanvi857.nioflow.protocol;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HttpResponseTest {

    @Test
    void status_int_setsCode() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.status(404);
        assertEquals(404, res.getStatus().getCode());
    }

    @Test
    void status_unknownCode_returnsInternalServerError() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.status(999);
        assertEquals(500, res.getStatus().getCode());
    }

    @Test
    void header_key_value_setsHeader() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.header("X-Test", "value");
        assertEquals("value", res.getHeadersMap().get("X-Test"));
    }

    @Test
    void header_duplicateKey_behaviourVerified() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.header("X-Test", "value1");
        res.header("X-Test", "value2");
        assertEquals("value2", res.getHeadersMap().get("X-Test")); // Overwrites
    }

    @Test
    void header_nullValue_handledGracefully() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.header("X-Test", null);
        assertFalse(res.getHeadersMap().containsKey("X-Test"));
    }

    @Test
    void json_object_setsContentTypeApplicationJson() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.json(Map.of("key", "value"));
        assertEquals("application/json", res.getHeadersMap().get("Content-Type"));
        assertTrue(new String(res.getBody()).contains("\"key\":\"value\""));
    }

    @Test
    void json_null_doesNotThrow() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        assertDoesNotThrow(() -> res.json(null));
    }

    @Test
    void send_string_setsBody() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.send("Hello World");
        assertEquals("Hello World", new String(res.getBody()));
        assertEquals("text/plain", res.getHeadersMap().get("Content-Type"));
    }

    @Test
    void send_null_doesNotThrow() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        assertDoesNotThrow(() -> res.send(null));
        assertEquals(0, res.getBody().length);
    }

    @Test
    void send_emptyString_setsEmptyBody() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.send("");
        assertEquals(0, res.getBody().length);
    }

    @Test
    void redirect_url_sets302AndLocationHeader() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.redirect("/login");
        assertEquals(302, res.getStatus().getCode());
        assertEquals("/login", res.getHeadersMap().get("Location"));
    }

    @Test
    void redirect_301_sets301() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.redirect("/permanent", 301);
        assertEquals(301, res.getStatus().getCode());
    }

    @Test
    void contentType_sets_contentTypeHeader() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        res.setContentType("text/html");
        assertEquals("text/html", res.getHeadersMap().get("Content-Type"));
    }

    @Test
    void writeTo_containsAllParts() throws IOException {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "Body Content");
        res.header("X-Custom", "Value");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        String output = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(output.startsWith("HTTP/1.1 200 OK"));
        assertTrue(output.contains("X-Custom: Value"));
        assertTrue(output.contains("Content-Length: 12"));
        assertTrue(output.endsWith("Body Content"));
    }

    @Test
    void writeTo_emptyStream_chunked() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        HttpResponse res = new HttpResponse(HttpStatus.OK, in, -1);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        String output = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(output.contains("Transfer-Encoding: chunked"));
        assertTrue(output.endsWith("0\r\n\r\n"));
    }

    @Test
    void writeTo_chunkedEncoding() throws IOException {
        String content = "Hello Chunked World";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        HttpResponse res = new HttpResponse(HttpStatus.OK, in, -1);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        String output = out.toString(StandardCharsets.UTF_8);
        
        assertTrue(output.contains("Transfer-Encoding: chunked"));
        assertTrue(output.contains(Integer.toHexString(content.length())));
        assertTrue(output.endsWith("0\r\n\r\n"));
    }

    @Test
    void writeTo_emptyBodyBytes() throws IOException {
        HttpResponse res = new HttpResponse(HttpStatus.OK, new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Content-Length: 0"));
    }

    @Test
    void header_nullKey_handled() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        // Should not throw NPE
        assertDoesNotThrow(() -> res.header(null, "value"));
    }

    @Test
    void writeTo_forceNullBody() throws Exception {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "");
        java.lang.reflect.Field field = HttpResponse.class.getDeclaredField("bodyBytes");
        field.setAccessible(true);
        field.set(res, null);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("HTTP/1.1 200 OK"));
    }

    @Test
    void writeTo_streamWithLength() throws IOException {
        String content = "Hello Stream World";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        HttpResponse res = new HttpResponse(HttpStatus.OK, in, content.length());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        String output = out.toString(StandardCharsets.UTF_8);
        
        assertFalse(output.contains("Transfer-Encoding: chunked"));
        assertTrue(output.contains("Content-Length: " + content.length()));
        assertTrue(output.endsWith(content));
    }

    @Test
    void toString_test() {
        HttpResponse res = new HttpResponse(HttpStatus.OK, "test");
        assertTrue(res.toString().contains("OK"));
    }
}

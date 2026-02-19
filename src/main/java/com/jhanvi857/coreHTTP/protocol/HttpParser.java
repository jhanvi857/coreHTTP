package com.jhanvi857.coreHTTP.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.jhanvi857.coreHTTP.exception.HttpParseException;

public final class HttpParser {

    public HttpRequest parse(InputStream in) throws Exception {
        // Reading headers
        String headerBlock = readHeaderBlock(in);
        if (headerBlock.isEmpty()) {
            throw new HttpParseException("Empty request");
        }

        String[] lines = headerBlock.split("\r\n");
        if (lines.length == 0) {
            throw new HttpParseException("Invalid HTTP request");
        }
        // Parsing Request Line
        String reqLine = lines[0];
        String[] parts = reqLine.split(" ");
        if (parts.length != 3) {
            throw new HttpParseException("Invalid request line: " + reqLine);
        }
        String method = parts[0];
        String path = parts[1];
        String version = parts[2];
        if (!path.startsWith("/")) {
            throw new HttpParseException("Invalid path: " + path);
        }

        if (!version.startsWith("HTTP/")) {
            throw new HttpParseException("Invalid HTTP version: " + version);
        }

        // Parsing Headers
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colonIndex = line.indexOf(":");
            if (colonIndex == -1) {
                continue;
            }
            String key = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();
            headers.put(key, value);
        }

        // Parsing Body
        byte[] body = new byte[0];
        if (headers.containsKey("Content-Length")) {
            try {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                if (contentLength > 0) {
                    body = readBody(in, contentLength);
                }
            } catch (NumberFormatException e) {
                throw new HttpParseException("Invalid Content-Length");
            }
        } else if (headers.containsKey("Transfer-Encoding")
                && "chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
            // TODO: Implement chunked encoding support
            throw new HttpParseException("Chunked encoding not supported yet");
        }

        return new HttpRequest(path, method, version, headers, body);
    }

    private String readHeaderBlock(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        int state = 0;
        // 0: nothing
        // 1: \r
        // 2: \r\n 3: \r\n\r

        while ((b = in.read()) != -1) {
            buffer.write(b);

            if (b == '\r') {
                if (state == 0)
                    state = 1;
                else if (state == 2)
                    state = 3;
                else
                    state = 0;
            } else if (b == '\n') {
                if (state == 1)
                    state = 2;
                else if (state == 3)
                    return buffer.toString(StandardCharsets.US_ASCII.name()).trim();
                else
                    state = 0;
            } else {
                state = 0;
            }

            // Safety limit for headers
            if (buffer.size() > 8192) {
                throw new IOException("Request headers too large");
            }
        }
        return buffer.toString(StandardCharsets.US_ASCII.name());
    }

    private byte[] readBody(InputStream in, int length) throws IOException {
        byte[] body = new byte[length];
        int totalRead = 0;
        int read;
        while (totalRead < length && (read = in.read(body, totalRead, length - totalRead)) != -1) {
            totalRead += read;
        }
        return body;
    }
}
package com.jhanvi857.coreHTTP.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.jhanvi857.coreHTTP.exception.HttpParseException;

public final class HttpParser {

    private static final int MAX_HEADER_SIZE_BYTES = 8192;
    private static final int MAX_CHUNK_LINE_BYTES = 1024;
    private static final int MAX_CHUNKED_BODY_BYTES = 10 * 1024 * 1024;

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
        String transferEncoding = getHeaderValueIgnoreCase(headers, "Transfer-Encoding");
        String contentLengthValue = getHeaderValueIgnoreCase(headers, "Content-Length");

        // We prioritize explicit framing rules to avoid ambiguous message bodies.
        // Accepting both Content-Length and chunked at once can open request-smuggling issues.
        if (transferEncoding != null) {
            if (!"chunked".equalsIgnoreCase(transferEncoding)) {
                throw new HttpParseException("Unsupported Transfer-Encoding: " + transferEncoding);
            }
            if (contentLengthValue != null) {
                throw new HttpParseException("Both Transfer-Encoding and Content-Length are not allowed");
            }
            body = readChunkedBody(in);
        } else if (contentLengthValue != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthValue);
                if (contentLength < 0) {
                    throw new HttpParseException("Negative Content-Length");
                }
                if (contentLength > 0) {
                    body = readBody(in, contentLength);
                }
            } catch (NumberFormatException e) {
                throw new HttpParseException("Invalid Content-Length");
            }
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
            if (buffer.size() > MAX_HEADER_SIZE_BYTES) {
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
        if (totalRead < length) {
            throw new IOException("Unexpected end of stream while reading request body");
        }
        return body;
    }

    private byte[] readChunkedBody(InputStream in) throws IOException {
        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();

        while (true) {
            String sizeLine = readLine(in, MAX_CHUNK_LINE_BYTES);
            if (sizeLine == null) {
                throw new IOException("Unexpected end of stream while reading chunk size");
            }

            String sizeToken = sizeLine;
            int extensionSeparator = sizeLine.indexOf(';');
            if (extensionSeparator >= 0) {
                sizeToken = sizeLine.substring(0, extensionSeparator);
            }
            sizeToken = sizeToken.trim();

            int chunkSize;
            try {
                chunkSize = Integer.parseInt(sizeToken, 16);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid chunk size: " + sizeLine);
            }

            if (chunkSize < 0) {
                throw new IOException("Negative chunk size");
            }

            if (chunkSize == 0) {
                // Trailer section ends with an empty line.
                // consuming it to leave stream aligned.
                while (true) {
                    String trailerLine = readLine(in, MAX_CHUNK_LINE_BYTES);
                    if (trailerLine == null || trailerLine.isEmpty()) {
                        return bodyBuffer.toByteArray();
                    }
                }
            }

            if (bodyBuffer.size() + chunkSize > MAX_CHUNKED_BODY_BYTES) {
                throw new IOException("Chunked body too large");
            }

            byte[] chunkData = readBody(in, chunkSize);
            bodyBuffer.write(chunkData);
            consumeRequiredCrlf(in);
        }
    }

    private void consumeRequiredCrlf(InputStream in) throws IOException {
        int first = in.read();
        int second = in.read();
        if (first != '\r' || second != '\n') {
            throw new IOException("Invalid chunk framing: missing CRLF after chunk data");
        }
    }

    private String readLine(InputStream in, int maxLineBytes) throws IOException {
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

        while (true) {
            int b = in.read();
            if (b == -1) {
                if (lineBuffer.size() == 0) {
                    return null;
                }
                throw new IOException("Unexpected end of stream while reading line");
            }

            if (lineBuffer.size() >= maxLineBytes) {
                throw new IOException("Line too long");
            }

            if (b == '\r') {
                int next = in.read();
                if (next == '\n') {
                    return lineBuffer.toString(StandardCharsets.US_ASCII.name());
                }
                throw new IOException("Invalid line ending");
            }

            lineBuffer.write(b);
        }
    }

    private String getHeaderValueIgnoreCase(Map<String, String> headers, String key) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return header.getValue();
            }
        }
        return null;
    }
}
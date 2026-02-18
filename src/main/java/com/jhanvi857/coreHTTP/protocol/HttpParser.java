package com.jhanvi857.coreHTTP.protocol;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.jhanvi857.coreHTTP.exception.HttpParseException;
public final class HttpParser {
    public HttpRequest parse(byte[] rawBytes) throws Exception {
        if (rawBytes == null || rawBytes.length == 0) {
            throw new HttpParseException("Empty string !");
        }
        String raw = new String(rawBytes,StandardCharsets.US_ASCII);
        String lines[] = raw.split("\r\n");
        if (lines.length == 0) {
            throw new HttpParseException("Invalid HTTP request");
        }
        String reqLine = lines[0];
        String parts[] = reqLine.split(" ");
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
        Map<String, String> headers = new HashMap<>();
        int i = 1;
        for (; i < lines.length; i++) {
            String line = lines[i];

            if (line.isEmpty()) {
                i++; // move to body
                break;
            }

            int colonIndex = line.indexOf(":");
            if (colonIndex == -1) {
                throw new HttpParseException("Malformed header: " + line);
            }

            String key = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();

            if (key.isEmpty()) {
                throw new HttpParseException("Empty header name");
            }

            headers.put(key, value);
        }
        String body = null;

        if (headers.containsKey("Content-Length")) {
            try {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                StringBuilder bodyBuilder = new StringBuilder();

                for (; i < lines.length; i++) {
                    bodyBuilder.append(lines[i]);
                    if (i != lines.length - 1) {
                        bodyBuilder.append("\r\n");
                    }
                }

                body = bodyBuilder.toString();

                if (body.length() != contentLength) {
                    throw new HttpParseException("Body length mismatch");
                }

            } catch (NumberFormatException e) {
                throw new HttpParseException("Invalid Content-Length");
            }
        }
        return new HttpRequest(path, method, version, headers, version);
    }
}
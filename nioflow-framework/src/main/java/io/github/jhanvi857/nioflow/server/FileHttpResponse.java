package io.github.jhanvi857.nioflow.server;

import io.github.jhanvi857.nioflow.protocol.HttpResponse;
import io.github.jhanvi857.nioflow.protocol.HttpStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

// specialized response that sends files using "Zero-Copy" logic.
public class FileHttpResponse extends HttpResponse {
    private final Path filePath;
    private final long fileSize;

    public FileHttpResponse(HttpStatus status, Path filePath, long fileSize) {
        // pass an empty byte array to the parent because we handle the body ourselves
        super(status, new byte[0]);
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        // 1. Write headers first.
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("HTTP/1.1 ")
                .append(getStatus().getCode()).append(" ")
                .append(getStatus().getMessage()).append("\r\n");

        for (Map.Entry<String, String> header : getHeadersMap().entrySet()) {
            headerBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        headerBuilder.append("\r\n");
        out.write(headerBuilder.toString().getBytes(StandardCharsets.UTF_8));

        // 2. USE zero-copy to send the file body
        // use transferTo to pump from disk to network directly.
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
                FileChannel fileChannel = fis.getChannel()) {

            long position = 0;
            while (position < fileSize) {
                // transferTo(position, count, destination) : tells the OS to handle the data
                // transfer directly.
                long transferred = fileChannel.transferTo(position, fileSize - position,
                        java.nio.channels.Channels.newChannel(out));
                if (transferred <= 0)
                    break;
                position += transferred;
            }
        }
        out.flush();
    }
}

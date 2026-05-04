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

public class FileHttpResponse extends HttpResponse {
    private final Path filePath;
    private final long fileSize;

    public FileHttpResponse(HttpStatus status, Path filePath, long fileSize) {
        super(status, new byte[0]);
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("HTTP/1.1 ")
                .append(getStatus().getCode()).append(" ")
                .append(getStatus().getMessage()).append("\r\n");

        for (Map.Entry<String, String> header : getHeadersMap().entrySet()) {
            headerBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        headerBuilder.append("\r\n");
        out.write(headerBuilder.toString().getBytes(StandardCharsets.UTF_8));

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
                FileChannel fileChannel = fis.getChannel()) {

            long position = 0;
            while (position < fileSize) {
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

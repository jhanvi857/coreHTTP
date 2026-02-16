package com.jhanvi857.coreHTTP.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println("Handling client : " + socket.getRemoteSocketAddress());
        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            bytesRead = in.read(buffer);

            if (bytesRead != -1) {
                String request = new String(buffer, 0, bytesRead);
                System.out.println(" RAW HTTP REQUEST ");
                System.out.println(request);
                System.out.println();
            }

            while ((bytesRead = in.read(buffer)) != -1) {
                System.out.println("Received " + bytesRead + "bytes from " + socket.getRemoteSocketAddress());
                String body = "Hello from coreHTTP";

                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        body;

                out.write(response.getBytes());
                out.flush();
            }
            System.out.println("Client closed connection from : " + socket.getRemoteSocketAddress());

        } catch (Exception e) {
            System.out.println("Error in connection handler : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                System.out.println("Exception in closing socket in connection handler." + e.getMessage());
            }
        }
    }
}
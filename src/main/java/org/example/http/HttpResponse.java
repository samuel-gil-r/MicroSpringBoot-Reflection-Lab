package org.example.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {

    private final OutputStream outputStream;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void sendText(String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        sendResponse("200 OK", "text/plain; charset=UTF-8", bodyBytes);
    }

    public void sendHtml(String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        sendResponse("200 OK", "text/html; charset=UTF-8", bodyBytes);
    }

    public void sendBytes(byte[] body, String contentType) throws IOException {
        sendResponse("200 OK", contentType, body);
    }

    public void sendNotFound() throws IOException {
        String body = "<h1>404 Not Found</h1>";
        sendResponse("404 Not Found", "text/html; charset=UTF-8", body.getBytes(StandardCharsets.UTF_8));
    }

    public void sendBadRequest(String message) throws IOException {
        String body = "<h1>400 Bad Request</h1><p>" + message + "</p>";
        sendResponse("400 Bad Request", "text/html; charset=UTF-8", body.getBytes(StandardCharsets.UTF_8));
    }

    public void sendInternalError(String message) throws IOException {
        String body = "<h1>500 Internal Server Error</h1><p>" + message + "</p>";
        sendResponse("500 Internal Server Error", "text/html; charset=UTF-8", body.getBytes(StandardCharsets.UTF_8));
    }

    private void sendResponse(String status, String contentType, byte[] body) throws IOException {
        String headers =
                "HTTP/1.1 " + status + "\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + body.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";

        outputStream.write(headers.getBytes(StandardCharsets.UTF_8));
        outputStream.write(body);
        outputStream.flush();
    }
}

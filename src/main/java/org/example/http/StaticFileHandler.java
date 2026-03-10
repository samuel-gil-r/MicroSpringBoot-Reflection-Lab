package org.example.http;

import java.io.IOException;
import java.io.InputStream;

public class StaticFileHandler {

    public static boolean handle(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        if (!isStaticFile(path)) {
            return false;
        }

        String resourcePath = "static" + path;

        InputStream resourceStream = StaticFileHandler.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            response.sendNotFound();
            return true;
        }

        byte[] content = resourceStream.readAllBytes();
        String contentType = getContentType(path);
        response.sendBytes(content, contentType);
        return true;
    }

    private static boolean isStaticFile(String path) {
        return path.endsWith(".html") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.equals("/");
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        return "application/octet-stream";
    }
}
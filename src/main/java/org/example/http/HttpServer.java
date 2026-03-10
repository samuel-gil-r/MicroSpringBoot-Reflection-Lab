package org.example.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

public class HttpServer {

    private final int port;
    private final Function<HttpRequest, String> dynamicHandler;

    public HttpServer(int port, Function<HttpRequest, String> dynamicHandler) {
        this.port = port;
        this.dynamicHandler = dynamicHandler;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor listo en http://localhost:" + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                Socket socket = clientSocket;
                var inputStream = socket.getInputStream();
                var outputStream = socket.getOutputStream()
        ) {
            HttpRequest request = HttpRequest.parse(inputStream);
            HttpResponse response = new HttpResponse(outputStream);

            if (request == null) {
                response.sendBadRequest("Solicitud inválida");
                return;
            }

            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                response.sendBadRequest("Solo se soporta GET");
                return;
            }

            boolean servedStatic = StaticFileHandler.handle(request, response);
            if (servedStatic) {
                return;
            }

            if (dynamicHandler != null) {
                String result = dynamicHandler.apply(request);

                if (result != null) {
                    response.sendText(result);
                } else {
                    response.sendNotFound();
                }
            } else {
                response.sendNotFound();
            }

        } catch (Exception e) {
            try {
                HttpResponse response = new HttpResponse(clientSocket.getOutputStream());
                response.sendInternalError(e.getMessage());
            } catch (IOException ignored) {
            }
        }
    }
}

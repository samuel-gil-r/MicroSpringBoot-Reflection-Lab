package org.example.framework;

import org.example.annotations.GetMapping;
import org.example.annotations.RequestParam;
import org.example.annotations.RestController;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MicroSpringBoot3 {

    public static Map<String, Method>  controllerMethods   = new HashMap<>();
    public static Map<String, Object>  controllerInstances = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        System.out.println("Loading rest controllers and their methods...");
        if (args.length > 0) {
            loadController(args[0]);
        } else {

            scanClasspath();
        }
        startServer(8080);
    }

    private static void loadController(String className) {
        try {
            Class c = Class.forName(className);

            if (c.isAnnotationPresent(RestController.class)) {

                Object instance = c.getDeclaredConstructor().newInstance();

                for (Method m : c.getDeclaredMethods()) {

                    if (m.isAnnotationPresent(GetMapping.class)) {

                        GetMapping a = m.getAnnotation(GetMapping.class);
                        String path = a.value();
                        controllerMethods.put(path, m);
                        controllerInstances.put(path, instance);
                        System.out.println("  Registered: GET " + path + " -> " + m.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load controller: " + className);
        }
    }


    private static void scanClasspath() {
        String classpath = System.getProperty("java.class.path");
        for (String cp : classpath.split(File.pathSeparator)) {
            File root = new File(cp);
            if (root.isDirectory()) {
                scanDirectory(root, root);
            }
        }
    }

    private static void scanDirectory(File root, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(root, f);
            } else if (f.getName().endsWith(".class")) {
                String relative  = root.toURI().relativize(f.toURI()).getPath();
                String className = relative.replace("/", ".").replace("\\", ".").replace(".class", "");
                try {
                    loadController(className);
                } catch (Exception ignored) {}
            }
        }
    }

    private static void startServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            System.out.println("Listo para recibir ...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (Exception e) {
                    System.err.println("Error handling request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException, InvocationTargetException, IllegalAccessException {
        OutputStream rawOut = clientSocket.getOutputStream();
        BufferedReader in   = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out     = new PrintWriter(rawOut, true);

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) return;

        System.out.println("Received: " + requestLine);


        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {}

        String[] tokens = requestLine.split(" ");
        String   rawUri = tokens[1];

        URI uri;
        try { uri = new URI(rawUri); }
        catch (Exception e) { sendError(out, 400, "Bad Request"); return; }

        String path  = uri.getPath();
        String query = uri.getQuery() != null ? uri.getQuery() : "";


        if (isStaticFile(path)) {
            serveStaticFile(path, rawOut, out);
            return;
        }

        if (controllerMethods.containsKey(path)) {
            Method m        = controllerMethods.get(path);
            Object instance = controllerInstances.get(path);
            Object[] params = resolveParams(m, query);
            String body     = (String) m.invoke(instance, params);
            sendOk(out, body, "text/plain");
            return;
        }


        sendError(out, 404, "Not Found: " + path);
    }


    private static boolean isStaticFile(String path) {
        return path.endsWith(".html") || path.endsWith(".htm")
                || path.endsWith(".png")  || path.endsWith(".jpg")
                || path.endsWith(".css")  || path.endsWith(".js")
                || path.endsWith(".ico");
    }

    private static void serveStaticFile(String path, OutputStream rawOut, PrintWriter out) throws IOException {
        String resourcePath = "/webroot" + path;
        InputStream fileStream = MicroSpringBoot3.class.getResourceAsStream(resourcePath);

        if (fileStream == null) {
            sendError(out, 404, "File Not Found: " + path);
            return;
        }

        byte[] fileBytes   = fileStream.readAllBytes();
        String contentType = getMimeType(path);
        fileStream.close();

        String header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: "   + contentType + "\r\n"
                + "Content-Length: " + fileBytes.length + "\r\n"
                + "\r\n";

        rawOut.write(header.getBytes());
        rawOut.write(fileBytes);
        rawOut.flush();
    }

    private static String getMimeType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }


    private static Object[] resolveParams(Method m, String query) {
        Map<String, String> queryParams = parseQuery(query);
        Parameter[] parameters = m.getParameters();
        Object[] values = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                values[i] = queryParams.getOrDefault(rp.value(), rp.defaultValue());
            }
        }
        return values;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
            else map.put(kv[0], "");
        }
        return map;
    }


    private static void sendOk(PrintWriter out, String body, String contentType) {
        out.print("HTTP/1.1 200 OK\r\n"
                + "Content-Type: "   + contentType + "\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body);
        out.flush();
    }

    private static void sendError(PrintWriter out, int code, String msg) {
        String body = code + " " + msg;
        out.print("HTTP/1.1 " + code + " " + msg + "\r\n"
                + "Content-Type: text/plain\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body);
        out.flush();
    }
}
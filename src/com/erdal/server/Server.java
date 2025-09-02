package com.erdal.server;

import com.erdal.repository.TaskRepository;
import com.erdal.model.Task;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Server {

    private static final int PORT = 8000;
    private static final String FRONTEND_PATH = "TaskManagement"; // frontend klasörün

    private final TaskRepository repo = new TaskRepository();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        //  API endpointleri
        server.createContext("/tasks", this::handleTasks);
        server.createContext("/add", this::handleAdd);
        server.createContext("/delete", this::handleDelete);

        //  Frontend dosyaları (index.html, style.css, script.js)
        server.createContext("/", this::handleFrontend);

        server.setExecutor(null);
        server.start();
        System.out.println(" Server started at http://localhost:" + PORT);
    }

    // 🔹 Tüm görevleri listele
    private void handleTasks(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        List<Task> tasks = repo.findAll();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            json.append(String.format("{\"id\":%d,\"title\":\"%s\",\"description\":\"%s\"}",
                    t.getId(), escapeJson(t.getTitle()), escapeJson(t.getDescription())));
            if (i < tasks.size() - 1) json.append(",");
        }
        json.append("]");

        sendJson(exchange, 200, json.toString());
    }

    //  Yeni görev ekle
    private void handleAdd(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseParams(body);

        String title = params.getOrDefault("title", "");
        String desc = params.getOrDefault("desc", "");

        if (!title.isEmpty()) {
            repo.add(new Task(title, desc));
            sendResponse(exchange, 200, "Added");
        } else {
            sendResponse(exchange, 400, "Title required");
        }
    }

    //  Görev sil
    private void handleDelete(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseParams(body);

        try {
            int id = Integer.parseInt(params.getOrDefault("id", "0"));
            boolean ok = repo.deleteById(id);
            sendResponse(exchange, ok ? 200 : 404, ok ? "Deleted" : "Not Found");
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid ID");
        }
    }

    // 🔹 Frontend dosyalarını yükle (index.html, css, js)
    private void handleFrontend(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html"; // default sayfa

        Path filePath = Paths.get(FRONTEND_PATH + path).normalize();
        if (!Files.exists(filePath)) {
            sendResponse(exchange, 404, "Not Found");
            return;
        }

        String mime = guessMimeType(path);
        byte[] bytes = Files.readAllBytes(filePath);

        exchange.getResponseHeaders().add("Content-Type", mime);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    //  Yardımcı metodlar
    private void sendResponse(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseParams(String body) {
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.put(key, val);
            }
        }
        return params;
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String guessMimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }
}

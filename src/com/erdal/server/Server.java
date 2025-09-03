package com.erdal.server;

import com.erdal.databaseConnection.DatabaseConnection;
import com.erdal.model.Task;
import com.erdal.methods.MethodService;
import com.erdal.methods.Methods;
import com.erdal.repository.TaskRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Server {
    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        System.out.println("✅ Server started at http://localhost:8000");

        // Backend endpoint
        server.createContext("/tasks", new TaskHandler());
        server.createContext("/add", new TaskHandler());
        server.createContext("/delete", new TaskHandler());
        server.createContext("/update", new TaskHandler());

        server.setExecutor(null); // default executor
        server.start();
    }
}

class TaskHandler implements HttpHandler {

    private final TaskRepository repo = new TaskRepository();
    private final MethodService methodService = new Methods(); // ID üretmek için

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/tasks") && method.equalsIgnoreCase("GET")) {
                handleGetTasks(exchange);
            } else if (path.equals("/add") && method.equalsIgnoreCase("GET")) {
                handleAddTask(exchange);
            } else if (path.equals("/delete") && method.equalsIgnoreCase("GET")) {
                handleDeleteTask(exchange);
            } else if (path.equals("/update") && method.equalsIgnoreCase("GET")) {
                handleUpdateTask(exchange);
            } else {
                sendResponse(exchange, "404 Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "500 Internal Server Error: " + e.getMessage());
        }
    }

    // 🔹 Listeleme (userId ile filtrelenmiş)
    private void handleGetTasks(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String userId = params.get("userId");
        if (userId == null) {
            sendResponse(exchange, "userId parametresi eksik");
            return;
        }

        List<Task> tasks = repo.findAllByUserId(userId);

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Task t : tasks) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("userId", t.getUserId());
            responseList.add(map);
        }

        sendJsonResponse(exchange, toJson(responseList));
    }

    // 🔹 Ekleme
    private void handleAddTask(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String title = params.get("title");
        String desc = params.get("desc");
        String userId = params.get("userId");

        if(title == null || title.isEmpty() || userId == null || userId.isEmpty()) {
            sendResponse(exchange, "Title ve userId zorunlu");
            return;
        }

        Task task = new Task(title, desc, userId);
        repo.add(task);
        sendResponse(exchange, "Task added successfully");
    }

    // 🔹 Silme
    private void handleDeleteTask(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        int id = Integer.parseInt(params.get("id"));
        String userId = params.get("userId");

        if (userId == null) {
            sendResponse(exchange, "userId parametresi eksik");
            return;
        }

        boolean ok = repo.deleteById(id, userId);
        sendResponse(exchange, ok ? "Task deleted successfully" : "Task not found / permission denied");
    }

    // 🔹 Güncelleme
    private void handleUpdateTask(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        int id = Integer.parseInt(params.get("id"));
        String title = params.get("title");
        String desc = params.get("desc");
        String userId = params.get("userId");

        if (userId == null) {
            sendResponse(exchange, "userId parametresi eksik");
            return;
        }

        boolean ok = repo.updateTitle(id, title, userId);
        sendResponse(exchange, ok ? "Task updated successfully" : "Task not found / permission denied");
    }

    private String toJson(List<Map<String, Object>> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Map<String, Object> t = tasks.get(i);
            sb.append("{")
                    .append("\"id\":").append(t.get("id")).append(",")
                    .append("\"title\":\"").append(t.get("title")).append("\",")
                    .append("\"description\":\"").append(t.get("description")).append("\",")
                    .append("\"userId\":\"").append(t.get("userId")).append("\"")
                    .append("}");
            if (i < tasks.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private Map<String, String> queryToMap(String query) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }
}

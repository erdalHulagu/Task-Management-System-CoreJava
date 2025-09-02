package com.erdal.server;

import com.erdal.databaseConnection.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
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

    // 🔹 Listeleme
    private void handleGetTasks(HttpExchange exchange) throws Exception {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, description FROM tasks ORDER BY id")) {

            List<Map<String, Object>> tasks = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", rs.getInt("id"));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                tasks.add(task);
            }

            String json = toJson(tasks);
            sendJsonResponse(exchange, json);
        }
    }

    // 🔹 Ekleme
    private void handleAddTask(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String title = params.get("title");
        String desc = params.get("desc");

        if(title == null || title.isEmpty()) {
            sendResponse(exchange, "Title is required");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "INSERT INTO tasks (title, description) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, desc);
            stmt.executeUpdate();
        }
        sendResponse(exchange, "Task added successfully");
    }

    // 🔹 Silme
    private void handleDeleteTask(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        int id = Integer.parseInt(params.get("id"));

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "DELETE FROM tasks WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        sendResponse(exchange, "Task deleted successfully");
    }

    // 🔹 Güncelleme
    private void handleUpdateTask(HttpExchange exchange) throws Exception {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        int id = Integer.parseInt(params.get("id"));
        String title = params.get("title");
        String desc = params.get("desc");

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "UPDATE tasks SET title = ?, description = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, desc);
            stmt.setInt(3, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                sendResponse(exchange, "Task updated successfully");
            } else {
                sendResponse(exchange, "Task not found");
            }
        }
    }

    // 🔹 JSON dönüşümü
    private String toJson(List<Map<String, Object>> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Map<String, Object> t = tasks.get(i);
            sb.append("{")
                    .append("\"id\":").append(t.get("id")).append(",")
                    .append("\"title\":\"").append(t.get("title")).append("\",")
                    .append("\"description\":\"").append(t.get("description")).append("\"")
                    .append("}");
            if (i < tasks.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // 🔹 Query parser
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

    // 🔹 Response helper
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

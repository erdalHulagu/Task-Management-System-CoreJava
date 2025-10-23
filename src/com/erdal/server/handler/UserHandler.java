package com.erdal.server.handler;

import com.erdal.model.User;
import com.erdal.repository.UserRepository;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UserHandler implements HttpHandler {

    private final UserRepository repo = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	 Headers h = exchange.getResponseHeaders();
    	    h.add("Access-Control-Allow-Origin", "*");
    	    h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");
    	    h.add("Access-Control-Allow-Headers", "Content-Type");
    	   
    	    // Preflight isteği
    	    if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
    	        exchange.sendResponseHeaders(204, -1); // 204 No Content
    	        return;
    	    }

    	    String method = exchange.getRequestMethod();
    	    String path = exchange.getRequestURI().getPath();
    	    String rawQuery = exchange.getRequestURI().getQuery();
    	    System.out.println("[UserHandler] " + method + " " + path + (rawQuery != null ? "?" + rawQuery : ""));

    	    try {
    	        if ("GET".equalsIgnoreCase(method) && "/user".equals(path)) {
    	            handleGet(exchange);
    	        } else if ("GET".equalsIgnoreCase(method) && "/updateUser".equals(path)) {
    	            Map<String, String> params = queryToMap(rawQuery);
    	            handleUpdateParams(exchange, params);
    	        } else if ("POST".equalsIgnoreCase(method) && ("/user".equals(path) || "/updateUser".equals(path))) {
    	            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    	            Map<String, String> params = queryToMap(body);
    	            handleUpdateParams(exchange, params);
    	        } else if ("DELETE".equalsIgnoreCase(method) && "/deleteUser".equals(path)) {
    	        	 exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    	             handleDeleteUser(exchange);
    	        } else {
    	            sendResponse(exchange, 405, "Method Not Allowed");
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        sendResponse(exchange, 500, "Internal Server Error");
    	    }
    }

    // 🔹 Kullanıcı bilgilerini getir
    private void handleGet(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        Map<String, String> params = queryToMap(requestURI.getQuery());
        String id = params.get("id");
        if (id == null) {
            sendResponse(exchange, 400, "Missing id");
            return;
        }

        User user = repo.findById(id);
        if (user == null) {
            sendResponse(exchange, 404, "User not found");
            return;
        }

        String json = String.format(
        	    "{\"id\":\"%s\",\"fullName\":\"%s\",\"phone\":\"%s\",\"gender\":\"%s\",\"address\":\"%s\",\"email\":\"%s\"}",
        	    escapeJson(user.getId()),
        	    escapeJson(user.getFullName()),
        	    escapeJson(user.getPhone()),
        	    escapeJson(user.getGender()),
        	    escapeJson(user.getAddress()),
        	    escapeJson(user.getEmail())
        	);


        exchange.getResponseHeaders().add("Content-Type", "application/json");
        sendResponse(exchange, 200, json);
    }

    // 🔹 Kullanıcı bilgilerini güncelle
    private void handleUpdateParams(HttpExchange exchange, Map<String, String> params) throws IOException {
        String id = params.get("id");
        String field = params.get("field");
        String value = params.get("value");

        System.out.println("[UserHandler] Update attempt -> id: " + id + " field: " + field + " value: " + value);

        if (id == null || field == null || value == null) {
            sendResponse(exchange, 400, "Eksik parametreler");
            return;
        }

        boolean success = repo.updateField(id, field, value);
        if (success) {
            sendResponse(exchange, 200, "OK");
        } else {
            sendResponse(exchange, 500, "Update failed");
        }
    }

    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String id = params.get("id");

        if (id == null || id.isEmpty()) {
            sendResponse(exchange, 400, "Missing user ID");
            return;
        }

        boolean deleted = repo.deleteUserById(id);

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if (deleted) {
            sendResponse(exchange, 200, "User deleted successfully");
        } else {
            sendResponse(exchange, 404, "User not found or could not be deleted");
        }
    }


    // 🔹 query veya body string (a=b&c=d) -> Map
    private Map<String, String> queryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) continue;
            String key = pair.substring(0, idx);
            String val = pair.substring(idx + 1);
            try {
                key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                val = URLDecoder.decode(val, StandardCharsets.UTF_8.name());
            } catch (Exception ignored) {
            }
            map.put(key, val);
        }
        return map;
    }

    // 🔹 HTTP yanıt gönder
    private void sendResponse(HttpExchange exchange, int statusCode, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 🔹 JSON escape
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

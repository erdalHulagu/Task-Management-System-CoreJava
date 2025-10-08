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
        // CORS ve header'lar
        Headers h = exchange.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
       


        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
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
                // update via query-string (backwards-compatible with your existing frontend)
                Map<String, String> params = queryToMap(rawQuery);
                handleUpdateParams(exchange, params);
            } else if ("POST".equalsIgnoreCase(method) && ("/user".equals(path) || "/updateUser".equals(path))) {
                // update via POST (body: application/x-www-form-urlencoded)
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = queryToMap(body);
                handleUpdateParams(exchange, params);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

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
                "{\"fullName\":\"%s\",\"phone\":\"%s\",\"gender\":\"%s\",\"address\":\"%s\",\"email\":\"%s\"}",
                escapeJson(user.getFullName()),
                escapeJson(user.getPhone()),
                escapeJson(user.getGender()),
                escapeJson(user.getAddress()),
                escapeJson(user.getEmail())
        );

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        sendResponse(exchange, 200, json);
    }

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

    // query veya body string (a=b&c=d) -> Map
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
            } catch (Exception e) {
                // ignore decoding error for now
            }
            map.put(key, val);
        }
        return map;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // küçük JSON escape (basit)
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

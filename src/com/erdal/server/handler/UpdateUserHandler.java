package com.erdal.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.erdal.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UpdateUserHandler implements HttpHandler {
    private final UserRepository repo = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery(); // id=xx&field=xx&value=xx
        if (query == null) {
            sendResponse(exchange, 400, "Missing parameters");
            return;
        }

        String[] params = query.split("&");
        String id = null, field = null, value = null;
        for (String p : params) {
            if (p.startsWith("id=")) id = p.substring(3);
            else if (p.startsWith("field=")) field = p.substring(6);
            else if (p.startsWith("value=")) value = p.substring(6);
        }

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

    private void sendResponse(HttpExchange exchange, int statusCode, String msg) throws IOException {
        exchange.sendResponseHeaders(statusCode, msg.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(msg.getBytes());
        }
    }
}

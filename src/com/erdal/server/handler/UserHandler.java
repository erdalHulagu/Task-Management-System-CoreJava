package com.erdal.server.handler;

import com.erdal.model.User;
import com.erdal.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class UserHandler implements HttpHandler {

    private final UserRepository repo = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS ayarları
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery(); // id=123
        String id = null;
        if (query != null && query.startsWith("id=")) {
            id = query.substring(3);
        }

        if (id == null) {
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().write("Missing id".getBytes());
            exchange.close();
            return;
        }

        User user = repo.findById(id);
        if (user == null) {
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().write("User not found".getBytes());
            exchange.close();
            return;
        }

        String json = String.format("{\"fullName\":\"%s\",\"phone\":\"%s\",\"gender\":\"%s\",\"address\":\"%s\",\"email\":\"%s\"}",
                user.getFullName(), user.getPhone(), user.getGender(), user.getAddress(), user.getEmail());

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}

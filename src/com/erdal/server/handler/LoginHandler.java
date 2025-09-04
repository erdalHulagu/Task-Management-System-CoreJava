package com.erdal.server.handler;

import com.erdal.model.Login;
import com.erdal.model.User;
import com.erdal.repository.LoginRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    private final LoginRepository loginRepo = new LoginRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String email = params.get("email");
        String password = params.get("password");

        if(email == null || password == null) {
            sendResponse(exchange, "Eksik bilgiler!", 400);
            return;
        }

        Login login = new Login(email, password);
        User user = loginRepo.authenticate(login);

        if(user != null) {
            String json = String.format("{\"id\":\"%s\", \"fullName\":\"%s\"}", user.getId(), user.getFullName());
            sendResponse(exchange, json, 200);
        } else {
            sendResponse(exchange, "Geçersiz email veya şifre", 401);
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if(query == null) return result;
        for(String param : query.split("&")) {
            String[] pair = param.split("=");
            if(pair.length > 1) {
                result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        try(OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

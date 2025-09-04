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
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, "Only POST method allowed");
            return;
        }

        Map<String, String> params = queryToMap(new String(exchange.getRequestBody().readAllBytes()));
        String email = params.get("email");
        String password = params.get("password");

        if (email == null || password == null) {
            sendResponse(exchange, "Email ve password gerekli");
            return;
        }

        Login login = new Login(email, password);
        User user = loginRepo.authenticate(login);

        if (user != null) {
            // Başarılı giriş, kullanıcı ID’sini dönebiliriz
            sendResponse(exchange, "Login successful;userId=" + user.getId());
        } else {
            sendResponse(exchange, "Geçersiz email veya şifre");
        }
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private Map<String, String> queryToMap(String query) throws IOException {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                result.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }
}


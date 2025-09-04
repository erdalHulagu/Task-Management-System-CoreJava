package com.erdal.server.handler;

import com.erdal.model.User;
import com.erdal.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RegisterHandler implements HttpHandler {

    private final UserRepository userRepo = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Sadece GET isteklerini kabul ediyoruz (daha sonra POST yapılabilir)
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, "Method not allowed");
            return;
        }

        // Query parametrelerini al
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());

        String fullName = params.get("fullName");
        String phone = params.get("phone");
        String gender = params.get("gender");
        String address = params.get("address");
        String email = params.get("email");
        String password = params.get("password");

        // Eksik alan kontrolü
        if(fullName == null || email == null || password == null) {
            sendResponse(exchange, "fullName, email ve password zorunlu");
            return;
        }

        // User nesnesi oluştur ve kaydet
        User user = new User(fullName, phone, gender, address, email, password);
        userRepo.register(user);

        sendResponse(exchange, "Kayıt başarılı! Giriş yapabilirsiniz.");
    }

    // Query string -> Map
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

    // Basit response gönderme
    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

package com.erdal.server.handler;

import com.erdal.email.EmailSender;
import com.erdal.email.EmailVerificationService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EmailVerificationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if ("/sendCode".equals(path)) {
            handleSendCode(exchange);
        } else if ("/verifyCode".equals(path)) {
            handleVerifyCode(exchange);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void handleSendCode(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String email = params.get("email");
        if (email == null) {
            sendResponse(exchange, 400, "Email eksik");
            return;
        }

        String code = EmailVerificationService.generateCode();
        EmailVerificationService.saveCode(email, code);

        boolean sent;
        try {
            EmailSender emailSender = new EmailSender("smtp.host.com", 587, "user@example.com", "password");
            sent = emailSender.sendVerificationCode(email, code);
        } catch (Exception e) {
            e.printStackTrace();
            sent = false;
        }

        if (sent) {
            sendResponse(exchange, 200, "Kod gönderildi");
        } else {
            sendResponse(exchange, 500, "Email gönderilemedi");
        }
    }

    private void handleVerifyCode(HttpExchange exchange) throws IOException {
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String email = params.get("email");
        String code = params.get("code");

        if (email == null || code == null) {
            sendResponse(exchange, 400, "Eksik parametre");
            return;
        }

        boolean valid = EmailVerificationService.verifyCode(email, code);
        if (valid) {
            sendResponse(exchange, 200, "Doğrulama başarılı");
        } else {
            sendResponse(exchange, 401, "Kod geçersiz");
        }
    }

    private Map<String, String> queryToMap(String query) {
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

    private void sendResponse(HttpExchange exchange, int code, String msg) throws IOException {
        byte[] bytes = msg.getBytes();
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

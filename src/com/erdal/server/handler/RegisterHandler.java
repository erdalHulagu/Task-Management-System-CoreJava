package com.erdal.server.handler;

import com.erdal.model.User;
import com.erdal.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class RegisterHandler implements HttpHandler {

    private final UserRepository repo = new UserRepository();
    private static final Map<String, String> verificationCodes = new HashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // 1️⃣ CORS için OPTIONS isteğine cevap
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCORSHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        addCORSHeaders(exchange);

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String action = params.get("action");

        if ("sendCode".equalsIgnoreCase(action)) {
            handleSendCode(exchange, params);
        } else if ("verify".equalsIgnoreCase(action)) {
            handleVerify(exchange, params);
        } else if ("register".equalsIgnoreCase(action)) {
            handleRegister(exchange, params);
        } else {
            sendResponse(exchange, "Geçersiz istek (action parametresi eksik)", 400);
        }
    }

    private void handleSendCode(HttpExchange exchange, Map<String, String> params) throws IOException {
        String email = params.get("email");
        if (email == null || email.isEmpty()) {
            sendResponse(exchange, "Email eksik!", 400);
            return;
        }

        // 6 haneli kod üret
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        verificationCodes.put(email, code);

        System.out.println("✅ Kod oluşturuldu: " + email + " -> " + code);

        try {
            sendEmail(email, code);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "Doğrulama kodu gönderilemedi!", 500);
            return;
        }

        sendResponse(exchange, "Doğrulama kodu gönderildi!", 200);
    }

    private void handleVerify(HttpExchange exchange, Map<String, String> params) throws IOException {
        String email = params.get("email");
        String inputCode = params.get("code");

        if (email == null || inputCode == null) {
            sendResponse(exchange, "Email veya kod eksik!", 400);
            return;
        }

        String correctCode = verificationCodes.get(email);
        if (correctCode != null && correctCode.equals(inputCode)) {
            sendResponse(exchange, "Kod doğru!", 200);
        } else {
            sendResponse(exchange, "Kod hatalı veya süresi doldu!", 403);
        }
    }

    private void handleRegister(HttpExchange exchange, Map<String, String> params) throws IOException {
        String fullName = params.get("fullName");
        String phone = params.get("phone");
        String gender = params.get("gender");
        String address = params.get("address");
        String email = params.get("email");
        String password = params.get("password");

        if (fullName == null || email == null || password == null) {
            sendResponse(exchange, "Gerekli bilgiler eksik!", 400);
            return;
        }

        User user = new User(fullName, phone, gender, address, email, password);
        repo.register(user);
        verificationCodes.remove(email);

        sendResponse(exchange, "Kayıt başarılı!", 200);
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                try {
                    result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name()),
                               URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void addCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendEmail(String toEmail, String code) throws MessagingException {
        // Gmail SMTP örneği
        String fromEmail = "seningmail@gmail.com";
        String password = "gmail_sifre"; // Uygulama şifresi önerilir

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Doğrulama Kodu");
        message.setText("Merhaba! Kayıt doğrulama kodunuz: " + code);

        Transport.send(message);
        System.out.println("📧 Email gönderildi: " + toEmail);
    }
}

package com.erdal.server;

import com.erdal.server.handler.EmailVerificationHandler;
import com.erdal.server.handler.LoginHandler;
import com.erdal.server.handler.RegisterHandler;
import com.erdal.server.handler.TaskHandler;
import com.erdal.server.handler.UserHandler;
import com.erdal.service.TaskReminderService; //  bunu ekle
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        System.out.println(" Server started at http://localhost:8000");

        // --- Context'leri ekliyoruz ---
        server.createContext("/Register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/tasks", new TaskHandler());
        server.createContext("/add", new TaskHandler());
        server.createContext("/delete", new TaskHandler());
        server.createContext("/update", new TaskHandler());
        server.createContext("/user", new UserHandler());
        server.createContext("/updateUser", new UserHandler());
        server.createContext("/sendCode", new EmailVerificationHandler());
        server.createContext("/verifyCode", new EmailVerificationHandler());

        // --- CORS Filtresi (TÜM endpointlere uygulanır) ---
        server.createContext("/", exchange -> {
            String response = "Server is running!";
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        // Her istek için global CORS filtresi (özellikle Register için)
        server.createContext("/Register", new RegisterHandler())
              .getFilters().add(new Filter() {
                  @Override
                  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
                      exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                      exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                      exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                          exchange.sendResponseHeaders(204, -1);
                          return;
                      }
                      chain.doFilter(exchange);
                  }

                  @Override
                  public String description() {
                      return "CORS filter for Register";
                  }
              });

        // --- 💡 Hatırlatma servisini başlatıyoruz ---
        new TaskReminderService().startDailyReminder();
        System.out.println("📧 Daily reminder service started!");

        // --- Sunucuyu başlat ---
        server.setExecutor(null);
        server.start();
    }

    public static void main(String[] args) throws Exception {
        start();
    }
}

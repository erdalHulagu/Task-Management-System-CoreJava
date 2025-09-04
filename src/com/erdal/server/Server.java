package com.erdal.server;

import com.erdal.server.handler.LoginHandler;
import com.erdal.server.handler.RegisterHandler;
import com.erdal.server.handler.TaskHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        System.out.println("✅ Server started at http://localhost:8000");

        // Task işlemleri için handler
        server.createContext("/tasks", new TaskHandler());
        server.createContext("/add", new TaskHandler());
        server.createContext("/delete", new TaskHandler());
        server.createContext("/update", new TaskHandler());

        // Kullanıcı kayıt işlemleri için handler
        server.createContext("/register", new RegisterHandler());

        // Gerekirse login için handler eklenebilir
        server.createContext("/login", new LoginHandler());

        server.setExecutor(null); // default executor
        server.start();
    }

    public static void main(String[] args) {
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

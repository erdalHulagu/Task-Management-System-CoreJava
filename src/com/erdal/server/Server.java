package com.erdal.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.erdal.model.Task;
import com.erdal.repository.TaskRepository;

public class Server {

    private final TaskRepository repo = new TaskRepository();

    public void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Task listeleme endpoint
        server.createContext("/tasks", new TaskListHandler());
        // Task ekleme endpoint
        server.createContext("/add", new TaskAddHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("Server started at http://localhost:8000");
    }

    // ✅ Task listeleme
    class TaskListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                List<Task> tasks = repo.findAll();
                StringBuilder sb = new StringBuilder("<ul>");
                for (Task t : tasks) {
                    sb.append("<li>ID: ").append(t.getId())
                      .append(", Başlık: ").append(t.getTitle())
                      .append(", Açıklama: ").append(t.getDescription())
                      .append("</li>");
                }
                sb.append("</ul>");
                byte[] response = sb.toString().getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ✅ Task ekleme (GET parametre ile)
    class TaskAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                String query = exchange.getRequestURI().getQuery();
                String title = "", desc = "";
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=");
                        if (kv.length == 2) {
                            if (kv[0].equals("title")) title = kv[1];
                            if (kv[0].equals("desc")) desc = kv[1];
                        }
                    }
                }

                if (!title.isEmpty()) {
                    repo.add(new Task(title, desc));
                }

                String response = "<p>Task eklendi! <a href=\"/tasks\">Listele</a></p>";
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

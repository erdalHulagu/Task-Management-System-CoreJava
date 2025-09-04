package com.erdal.server.handler;

import com.erdal.model.Task;
import com.erdal.repository.TaskRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TaskHandler implements HttpHandler {

    private final TaskRepository repo = new TaskRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());

        try {
            switch(path) {
                case "/tasks":
                    handleGetTasks(exchange, params);
                    break;
                case "/add":
                    handleAddTask(exchange, params);
                    break;
                case "/delete":
                    handleDeleteTask(exchange, params);
                    break;
                case "/update":
                    handleUpdateTask(exchange, params);
                    break;
                default:
                    sendResponse(exchange, "404 Not Found", 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "500 Internal Server Error: " + e.getMessage(), 500);
        }
    }

    private void handleGetTasks(HttpExchange exchange, Map<String,String> params) throws IOException {
        String userId = params.get("userId");
        if(userId == null) { sendResponse(exchange, "userId eksik", 400); return; }

        List<Task> tasks = repo.findAllByUserId(userId);
        List<Map<String,Object>> list = new ArrayList<>();
        for(Task t : tasks) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("userId", t.getUserId());
            list.add(map);
        }

        sendResponse(exchange, toJson(list), 200);
    }

    private void handleAddTask(HttpExchange exchange, Map<String,String> params) throws IOException {
        String title = params.get("title");
        String desc = params.get("desc");
        String userId = params.get("userId");

        if(title == null || userId == null) { sendResponse(exchange, "title ve userId gerekli", 400); return; }

        Task task = new Task(title, desc, userId);
        repo.add(task);
        sendResponse(exchange, "Task added", 200);
    }

    private void handleDeleteTask(HttpExchange exchange, Map<String,String> params) throws IOException {
        String userId = params.get("userId");
        int id = Integer.parseInt(params.get("id"));

        boolean ok = repo.deleteById(id, userId);
        sendResponse(exchange, ok ? "Task deleted" : "Not found / permission denied", 200);
    }

    private void handleUpdateTask(HttpExchange exchange, Map<String,String> params) throws IOException {
        String userId = params.get("userId");
        int id = Integer.parseInt(params.get("id"));
        String title = params.get("title");
        String desc = params.get("desc");

        boolean ok = repo.updateTitle(id, title, userId);
        sendResponse(exchange, ok ? "Task updated" : "Not found / permission denied", 200);
    }

    private String toJson(List<Map<String,Object>> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for(int i=0;i<tasks.size();i++) {
            Map<String,Object> t = tasks.get(i);
            sb.append("{\"id\":").append(t.get("id"))
              .append(",\"title\":\"").append(t.get("title"))
              .append("\",\"description\":\"").append(t.get("description"))
              .append("\",\"userId\":\"").append(t.get("userId")).append("\"}");
            if(i<tasks.size()-1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private Map<String,String> queryToMap(String query) {
        Map<String,String> map = new HashMap<>();
        if(query == null) return map;
        for(String param : query.split("&")) {
            String[] pair = param.split("=");
            if(pair.length>1) map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                                      URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }
        return map;
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin","*");
        exchange.getResponseHeaders().add("Content-Type","application/json");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        try(OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }
    }
}

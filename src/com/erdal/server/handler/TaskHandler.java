package com.erdal.server.handler;

import com.erdal.model.Task;
import com.erdal.repository.TaskRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
            map.put("taskTime", t.getTaskTime() != null ? t.getTaskTime().toString() : "");

            list.add(map);
        }

        sendResponse(exchange, toJson(list), 200);
    }

    private void handleAddTask(HttpExchange exchange, Map<String, String> params) throws IOException {
        String title = params.get("title");
        String desc = params.get("desc");
        String userId = params.get("userId");

        // Frontend’ten "date" olarak geliyor, bu yüzden onu alıyoruz
        String timeParam = params.get("date");
        if (timeParam == null) {
            timeParam = params.get("taskTime"); // yedek: eğer ileride parametre adı değişirse
        }

        if (title == null || userId == null) {
            sendResponse(exchange, "title ve userId gerekli", 400);
            return;
        }

        LocalDate taskTime = null;
        if (timeParam != null && !timeParam.isEmpty()) {
            try {
                taskTime = LocalDate.parse(timeParam);
            } catch (DateTimeParseException e) {
                sendResponse(exchange, "Geçersiz tarih formatı. Beklenen format: yyyy-MM-dd", 400);
                return;
            }
        }

        Task task = new Task(title, desc, userId, taskTime);
        repo.add(task);

        // Email simülasyonu
        if (taskTime != null && taskTime.equals(LocalDate.now())) {
            System.out.println("Email gönderildi: Görev bugün yapılacak -> " + task.getTitle());
        }

        sendResponse(exchange, "Task added", 200);
    }
    private void handleDeleteTask(HttpExchange exchange, Map<String,String> params) throws IOException {
        String userId = params.get("userId");
        int id = Integer.parseInt(params.get("id"));

        boolean ok = repo.deleteById(id, userId);
        sendResponse(exchange, ok ? "Task deleted" : "Not found / permission denied", 200);
    }

    private void handleUpdateTask(HttpExchange exchange, Map<String, String> params) throws IOException {
        String userId = params.get("userId");
        String idStr = params.get("id");
        String title = params.get("title");
        String desc = params.get("desc"); // şimdilik kullanılmıyor
        String timeParam = params.get("taskTime");

        if (userId == null || idStr == null || title == null) {
            sendResponse(exchange, "userId, id ve title gerekli", 400);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendResponse(exchange, "Geçersiz id formatı", 400);
            return;
        }

        LocalDate taskTime = null;
        if (timeParam != null && !timeParam.isEmpty()) {
            try {
                taskTime = LocalDate.parse(timeParam);
            } catch (DateTimeParseException e) {
                sendResponse(exchange, "Geçersiz tarih formatı. Beklenen: yyyy-MM-dd", 400);
                return;
            }
        }

        boolean ok = repo.updateTitleDescAndTime(id, title, desc,taskTime, userId);

        // Email simülasyonu
        if(taskTime != null && taskTime.equals(LocalDate.now()) && ok) {
            System.out.println("Email gönderildi: Görev bugün yapılacak -> " + title);
        }

        sendResponse(exchange, ok ? "Task updated" : "Not found / permission denied", 200);
    }

    private String toJson(List<Map<String,Object>> tasks) {
        StringBuilder sb = new StringBuilder("[");
        for(int i=0;i<tasks.size();i++) {
            Map<String,Object> t = tasks.get(i);
            sb.append("{\"id\":").append(t.get("id"))
              .append(",\"title\":\"").append(t.get("title"))
              .append("\",\"description\":\"").append(t.get("description"))
              .append("\",\"userId\":\"").append(t.get("userId"))
              .append("\",\"taskTime\":\"").append(t.get("taskTime")).append("\"}");
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

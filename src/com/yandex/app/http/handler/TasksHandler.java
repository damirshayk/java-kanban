package com.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;
import com.yandex.app.http.HttpTaskServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик HTTP-запросов для пути /tasks.
 * Поддерживает методы GET, POST и DELETE для работы с задачами.
 */
public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendResponse(exchange, "", 405);
            }
        } catch (Exception e) {
            // Любая неожиданная ошибка
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        // /tasks или /tasks/{id}
        if (parts.length == 2) { // /tasks
            String json = gson.toJson(manager.getAllTasks());
            sendResponse(exchange, json, 200);
            return;
        }
        if (parts.length == 3) { // /tasks/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                var optTask = manager.getTaskById(id);
                if (optTask.isPresent()) {
                    String json = gson.toJson(optTask.get());
                    sendResponse(exchange, json, 200);
                } else {
                    sendNotFound(exchange, "Task с id " + id + " не найден");                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный task id");
            }
            return;
        }
        // Для любых других путей — 404
        sendNotFound(exchange, "Не найден");
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        // читаем тело запроса
        byte[] bytes;
        try (InputStream is = exchange.getRequestBody()) {
            bytes = is.readAllBytes();
        }
        String body = new String(bytes, StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);
        if (task == null) {
            sendServerError(exchange, "Неверный task body");
            return;
        }
        try {
            // Если id <= 0 или не указан, считаем что задача новая
            if (task.getId() <= 0) {
                manager.addTask(task);
            } else {
                manager.updateTask(task);
            }
            // В случае успешного создания/обновления ничего не возвращаем
            sendEmpty(exchange, 201);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() != null ? e.getMessage() : "";
            if (message.contains("пересекается")) {
                sendHasOverlap(exchange, message);
            } else {
                sendNotFound(exchange, message);
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 3) { // /tasks/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                try {
                    manager.deleteTaskById(id);
                    sendEmpty(exchange, 200);
                } catch (IllegalArgumentException e) {
                    sendNotFound(exchange, e.getMessage());
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный task id");
            }
            return;
        }
        // Удаление всех задач не предусмотрено API — 404
        sendNotFound(exchange, "Не найден");
    }
}

package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.model.Subtask;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик HTTP-запросов для подзадач (subtasks).
 * Поддерживает методы GET, POST и DELETE для работы с подзадачами.
 */
public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager manager) {
        super(manager);
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
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        // /subtasks или /subtasks/{id}
        if (parts.length == 2) {
            String json = gson.toJson(manager.getAllSubtasks());
            sendResponse(exchange, json, 200);
            return;
        }
        if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);
                var opt = manager.getSubtaskById(id);
                if (opt.isPresent()) {
                    String json = gson.toJson(opt.get());
                    sendResponse(exchange, json, 200);
                } else {
                    sendNotFound(exchange, "Подзадача с id " + id + " не найдена");
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный subtask id");
            }
            return;
        }
        sendNotFound(exchange, "Не найден");
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        byte[] bytes;
        try (InputStream is = exchange.getRequestBody()) {
            bytes = is.readAllBytes();
        }
        String body = new String(bytes, StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        if (subtask == null) {
            sendServerError(exchange, "Неверный subtask body");
            return;
        }
        try {
            if (subtask.getId() <= 0) {
                manager.addSubtask(subtask);
            } else {
                manager.updateSubtask(subtask);
            }
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
        if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);
                try {
                    manager.deleteSubtaskById(id);
                    sendEmpty(exchange, 200);
                } catch (IllegalArgumentException e) {
                    sendNotFound(exchange, e.getMessage());
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный subtask id");
            }
            return;
        }
        sendNotFound(exchange, "Не найден");
    }
}
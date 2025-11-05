package com.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Epic;
import com.yandex.app.service.TaskManager;
import com.yandex.app.http.HttpTaskServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик HTTP-запросов для эпиков.
 * Поддерживает методы GET, POST и DELETE.
 */
public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager) {
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
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        // /epics, /epics/{id}, /epics/{id}/subtasks
        if (parts.length == 2) {
            String json = gson.toJson(manager.getAllEpics());
            sendResponse(exchange, json, 200);
            return;
        }
        if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);
                var opt = manager.getEpicById(id);
                if (opt.isPresent()) {
                    String json = gson.toJson(opt.get());
                    sendResponse(exchange, json, 200);
                } else {
                    sendNotFound(exchange, "Эпик с id " + id + " не найден");
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный epic id");
            }
            return;
        }
        if (parts.length == 4 && "subtasks".equals(parts[3])) {
            try {
                int id = Integer.parseInt(parts[2]);
                var subs = manager.getSubtasksOfEpic(id);
                // Если эпик не найден, getSubtasksOfEpic вернёт пустой список. Однако согласно
                // спецификации следует возвращать 404, если эпика нет.
                if (manager.getEpicById(id).isEmpty()) {
                    sendNotFound(exchange, "Эпик с id " + id + " не найден");
                    return;
                }
                String json = gson.toJson(subs);
                sendResponse(exchange, json, 200);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный epic id");
            }
            return;
        }
        sendNotFound(exchange, "Не найден");
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        // Создаёт новый эпик. Обновление эпика через POST не поддерживается.
        byte[] bytes;
        try (InputStream is = exchange.getRequestBody()) {
            bytes = is.readAllBytes();
        }
        String body = new String(bytes, StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);
        if (epic == null) {
            sendServerError(exchange, "Неверный epic body");
            return;
        }
        try {
            // Всегда создаём новую сущность. Id из клиента игнорируется.
            epic.setId(0);
            manager.addEpic(epic);
            sendEmpty(exchange, 201);
        } catch (IllegalArgumentException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 3) {
            try {
                int id = Integer.parseInt(parts[2]);
                try {
                    manager.deleteEpicById(id);
                    sendEmpty(exchange, 200);
                } catch (IllegalArgumentException e) {
                    sendNotFound(exchange, e.getMessage());
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный epic id");
            }
            return;
        }
        sendNotFound(exchange, "Не найден");
    }
}

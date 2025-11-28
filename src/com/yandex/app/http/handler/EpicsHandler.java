package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.model.Epic;
import com.yandex.app.service.EpicNotFoundException;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик HTTP-запросов для эпиков.
 * Поддерживает методы GET, POST и DELETE.
 */
public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager) {
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
            sendServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает GET-запросы.
     * Поддерживаются следующие пути:
     * - /epics — получить все эпики
     * - /epics/{id} — получить эпик по ID
     * - /epics/{id}/subtasks — получить все подзадачи эпика по ID
     *
     * @param exchange объект обмена HTTP
     * @param path     путь запроса
     * @throws IOException при ошибке записи ответа
     */
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
                var subs = manager.getSubtasksOfEpic(id); // здесь уже бросится исключение, если эпика нет
                String json = gson.toJson(subs);
                sendResponse(exchange, json, 200);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный epic id");
            } catch (EpicNotFoundException e) {
                // Эпик не найден → 404
                sendNotFound(exchange, e.getMessage());
            }
            return;
        }
        sendNotFound(exchange, "Не найден");
    }

    /**
     * Обрабатывает POST-запросы для создания нового эпика.
     * Обновление эпика через POST не поддерживается.
     *
     * @param exchange объект обмена HTTP
     * @throws IOException при ошибке чтения тела запроса или записи ответа
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        // Создаёт новый эпик. Обновление эпика через POST не поддерживается.
        byte[] bytes;
        try (InputStream is = exchange.getRequestBody()) {
            bytes = is.readAllBytes();
        }

        String body = new String(bytes, StandardCharsets.UTF_8);

        if (body.isBlank()) {
            sendServerError(exchange, "Пустое тело запроса для epic");
            return;
        }

        Epic epic = gson.fromJson(body, Epic.class);
        if (epic == null) {
            sendServerError(exchange, "Неверный epic body");
            return;
        }

        try {
            // Если id <= 0 или не указан — создаём новый эпик
            if (epic.getId() <= 0) {
                manager.addEpic(epic);
            } else {
                // Если id передан — обновляем существующий эпик
                manager.updateEpic(epic);
            }
            sendEmpty(exchange, 201);
        } catch (EpicNotFoundException  e) {
            String message = e.getMessage() != null ? e.getMessage() : ""; // защита от NPE
            sendNotFound(exchange, message);
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

package com.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.net.URI;

/**
 * Обработчик HTTP‑запросов для пути /history.
 * Поддерживает только метод GET, возвращающий историю просмотров задач.
 * Любые другие методы возвращают статус 405 (Метод не поддерживается).
 */
public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        if (!"/history".equals(path)) {
            sendNotFound(exchange, "Неверный путь: " + path);
            return;
        }
        if ("GET".equals(method)) {
            // Получаем историю и сериализуем её в JSON
            String json = gson.toJson(manager.getHistory());
            sendResponse(exchange, json, 200);
        } else {
            sendResponse(exchange, "Метод не поддерживается", 405);
        }
    }
}
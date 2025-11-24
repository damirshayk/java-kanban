package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.net.URI;

/**
 * Обработчик HTTP‑запросов для пути /prioritized.
 * Поддерживает только метод GET, возвращающий список задач в порядке приоритета (по времени начала).
 * Если запрос выполнен успешно, возвращается статус 200 и JSON‑массив задач.
 * Если метод не поддерживается, возвращается 405.
 */
public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        if (!"/prioritized".equals(path)) {
            sendNotFound(exchange, "Неверный путь: " + path);
            return;
        }
        if ("GET".equals(method)) {
            // Возвращаем список задач в порядке приоритета
            String json = gson.toJson(manager.getPrioritizedTasks());
            sendResponse(exchange, json, 200);
        } else {
            // Метод не поддерживается
            sendResponse(exchange, "", 405);
        }
    }
}

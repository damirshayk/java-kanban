package com.yandex.app.http.handler;


import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.http.HttpTaskServer;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Базовый HTTP‑обработчик. Содержит методы для отправки ответов с нужным статусом и
 * минимизирует дублирование кода между конкретными обработчиками. Все ответы
 * кодируются в UTF‑8 и имеют тип «application/json».
 */
public abstract class BaseHttpHandler implements HttpHandler {

    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager must not be null");
        this.gson = Objects.requireNonNull(HttpTaskServer.getGson(), "gson must not be null");
    }

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    /**
     * Отправляет строковый ответ клиенту. В методе устанавливаются заголовок Content‑Type и статус ответа.
     * Если тело пустое, всё равно отправляется пустая строка, чтобы корректно закрыть поток.
     *
     * @param exchange объект обмена HTTP
     * @param response тело ответа
     * @param status   HTTP‑статус
     * @throws IOException при ошибке записи ответа
     */
    protected void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
        if (response == null) {
            response = "";
        }
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Отправляет ответ без тела. Нужен для запросов, в которых нет содержимого,
     * например при успешном создании или удалении ресурса.
     * Использует -1 в качестве длины тела, чтобы не ожидать данные в потоке.
     *
     * @param exchange объект обмена HTTP
     * @param status   HTTP‑статус
     * @throws IOException при ошибке отправки
     */
    protected void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    /**
     * Возвращает код 404.
     * Используется, когда запрашиваемый ресурс отсутствует.
     *
     * @param exchange объект обмена HTTP
     * @param message  текст ошибки
     * @throws IOException при ошибке отправки
     */
    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, message, 404);
    }

    /**
     * Возвращает код 406.
     * Используется, когда создаваемая или обновляемая сущность пересекается по времени с уже существующей.
     *
     * @param exchange объект обмена HTTP
     * @param message  текст ошибки
     * @throws IOException при ошибке отправки
     */
    protected void sendHasOverlap(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, message, 406);
    }

    /**
     * Возвращает код 500 (Internal Server Error).
     * Используется, когда происходит непредвиденная ошибка при обработке запроса.
     *
     * @param exchange объект обмена HTTP
     * @param message  текст ошибки
     * @throws IOException при ошибке отправки
     */
    protected void sendServerError(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, message, 500);
    }
}

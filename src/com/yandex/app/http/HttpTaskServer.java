package com.yandex.app.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.app.http.adapter.DurationAdapter;
import com.yandex.app.http.adapter.LocalDateTimeAdapter;
import com.yandex.app.http.handler.EpicsHandler;
import com.yandex.app.http.handler.HistoryHandler;
import com.yandex.app.http.handler.PrioritizedHandler;
import com.yandex.app.http.handler.SubtasksHandler;
import com.yandex.app.http.handler.TasksHandler;
import com.yandex.app.service.Managers;
import com.yandex.app.service.TaskManager;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * HTTP-сервер для трекера задач. Принимает запросы на порт 8080 и
 * делегирует их соответствующим обработчикам. Каждый путь соответствует
 * группе методов TaskManager.
 */
public class HttpTaskServer {

    private static final int PORT = 8080;

    private final HttpServer server;

    private static final Gson GSON = new GsonBuilder()
            // Регистрируем адаптеры для Duration и LocalDateTime, чтобы корректно сериализовать
            // и десериализовать эти типы (в JSON Duration представляется количеством минут,
            // а LocalDateTime — в формате ISO_LOCAL_DATE_TIME).
            .registerTypeAdapter(java.time.Duration.class, new DurationAdapter())
            .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Создаёт HTTP-сервер. Для корректной работы необходимо передать
     * существующий менеджер задач. Тесты могут использовать InMemoryTaskManager,
     * а основной метод main — FileBackedTaskManager через Managers.getDefault().
     *
     * @param manager менеджер задач
     * @throws IOException если не удаётся открыть порт
     */
    public HttpTaskServer(TaskManager manager) throws IOException {
        TaskManager safeManager = Objects.requireNonNull(manager, "manager must not be null");
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // привязываем обработчики к путям
        server.createContext("/tasks", new TasksHandler(safeManager));
        server.createContext("/subtasks", new SubtasksHandler(safeManager));
        server.createContext("/epics", new EpicsHandler(safeManager));
        server.createContext("/history", new HistoryHandler(safeManager));
        server.createContext("/prioritized", new PrioritizedHandler(safeManager));
    }

    /**
     * Возвращает экземпляр Gson, используемый сервером для
     * сериализации/десериализации. Нужен в тестах и базовом HTTP-обработчике.
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Запускает HTTP-сервер. После вызова метода сервер начинает
     * обрабатывать входящие запросы.
     */
    public void start() {
        server.start();
    }

    /**
     * Останавливает HTTP-сервер. После вызова метода сервер перестаёт
     * принимать запросы. Параметр 0 означает немедленное завершение.
     */
    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer httpServer = new HttpTaskServer(manager);
        httpServer.start();
        System.out.println("HTTP сервер запущен на порту " + PORT);
    }
}
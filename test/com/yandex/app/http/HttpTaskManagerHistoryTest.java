package com.yandex.app.http;

import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для эндпоинта /history.
 * Проверяет, что история корректно формируется при обращении по API,
 * что нет дубликатов и что удалённые задачи исчезают из истории.
 */
public class HttpTaskManagerHistoryTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testHistoryOrder() throws IOException, InterruptedException {
        Task t1 = new Task("t1", "d", TaskStatus.NEW);
        Task t2 = new Task("t2", "d", TaskStatus.NEW);
        manager.addTask(t1);
        manager.addTask(t2);
        // обращаемся к задачам через HTTP, чтобы история записалась
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t1.getId()))
                .GET()
                .build();
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t2.getId()))
                .GET()
                .build();
        client.send(req1, HttpResponse.BodyHandlers.ofString());
        client.send(req2, HttpResponse.BodyHandlers.ofString());
        // запрашиваем историю
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());
        Task[] history = HttpTaskServer.getGson().fromJson(historyResponse.body(), Task[].class);
        assertEquals(2, history.length);
        assertEquals(t1.getId(), history[0].getId());
        assertEquals(t2.getId(), history[1].getId());
    }

    @Test
    public void testHistoryNoDuplicates() throws IOException, InterruptedException {
        Task t = new Task("NoDuplicates", "d", TaskStatus.NEW);
        manager.addTask(t);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t.getId()))
                .GET()
                .build();
        client.send(req, HttpResponse.BodyHandlers.ofString());
        client.send(req, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> historyResponse =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/history"))
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofString());
        Task[] history = HttpTaskServer
                .getGson()
                .fromJson(historyResponse.body(), Task[].class);
        assertEquals(1, history.length, "История не должна содержать дубликаты");
    }

    @Test
    public void testHistoryRemovesDeleted() throws IOException, InterruptedException {
        Task t = new Task("RemovesDeleted", "d", TaskStatus.NEW);
        manager.addTask(t);
        // обращаемся через HTTP, чтобы добавить в историю
        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t.getId()))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        // удаляем через API
        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t.getId()))
                .DELETE()
                .build(), HttpResponse.BodyHandlers.ofString());
        // проверяем историю
        HttpResponse<String> historyResponse = client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        Task[] history = HttpTaskServer.getGson()
                .fromJson(historyResponse.body(), Task[].class);
        assertEquals(0, history.length, "Удалённая задача должна исчезнуть из истории");
    }
}
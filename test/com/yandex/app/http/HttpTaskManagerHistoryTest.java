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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тесты для эндпоинта /history.
 * В каждом тесте через HTTP вызывается только GET /history.
 * Заполнение истории выполняется через TaskManager.
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

    /**
     * Проверяет, что задачи в истории возвращаются в правильном порядке.
     */
    @Test
    public void testHistoryOrder() throws IOException, InterruptedException {

        Task t1 = new Task("t1", "d", TaskStatus.NEW);
        Task t2 = new Task("t2", "d", TaskStatus.NEW);
        manager.addTask(t1);
        manager.addTask(t2);

        // наполняем историю
        manager.getTaskById(t1.getId());
        manager.getTaskById(t2.getId());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build(); // запрос GET /history
        HttpResponse<String> historyResponse =
                client.send(historyRequest, HttpResponse.BodyHandlers.ofString()); // выполняем запрос

        assertEquals(200, historyResponse.statusCode()); // Проверяем, что статус ответа 200 OK
        Task[] history = HttpTaskServer.getGson()
                .fromJson(historyResponse.body(), Task[].class); // Парсим тело ответа в массив задач
        assertNotNull(history); // Проверяем, что тело ответа не null
        assertEquals(2, history.length); // Проверяем, что в истории 2 задачи
        assertEquals(t1.getId(), history[0].getId()); //  Проверяем, что первая задача в истории - t1
        assertEquals(t2.getId(), history[1].getId()); //  Проверяем, что вторая задача в истории - t2
    }

    /**
     * Проверяет, что в истории не появляются дубликаты при повторном просмотре задачи.
     */
    @Test
    public void testHistoryNoDuplicates() throws IOException, InterruptedException {
        Task t = new Task("NoDuplicates", "d", TaskStatus.NEW);
        manager.addTask(t);

        manager.getTaskById(t.getId());
        manager.getTaskById(t.getId()); // повторный просмотр

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build(); // запрос GET /history
        HttpResponse<String> historyResponse =
                client.send(historyRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, historyResponse.statusCode()); // Проверяем, что статус ответа 200 OK
        Task[] history = HttpTaskServer.getGson()
                .fromJson(historyResponse.body(), Task[].class);
        assertNotNull(history); // Проверяем, что тело ответа не null
        assertEquals(1, history.length,
                "История не должна содержать дубликаты"); // Проверяем, что в истории только одна задача
        assertEquals(t.getId(), history[0].getId()); // Проверяем, что задача в истории - t
    }


    /**
     * Проверяет, что удалённые задачи исчезают из истории.
     */
    @Test
    public void testHistoryRemovesDeleted() throws IOException, InterruptedException {
        Task t = new Task("RemovesDeleted", "d", TaskStatus.NEW);
        manager.addTask(t); // добавляем задачу

        manager.getTaskById(t.getId()); // добавляем в историю
        manager.deleteTaskById(t.getId()); // удаляем задачу

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build(); // запрос GET /history
        HttpResponse<String> historyResponse =
                client.send(historyRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, historyResponse.statusCode()); // Проверяем, что статус ответа 200 OK
        Task[] history = HttpTaskServer.getGson()
                .fromJson(historyResponse.body(), Task[].class);
        assertNotNull(history); // Проверяем, что тело ответа не null
        assertEquals(0, history.length,
                "Удалённая задача должна исчезнуть из истории"); // Проверяем, что история пуста
    }
}
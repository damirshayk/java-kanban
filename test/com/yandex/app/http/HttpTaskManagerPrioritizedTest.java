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
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для эндпоинта /prioritized.
 * Проверяется корректность сортировки и обработки задач без времени начала.
 * Используется InMemoryTaskManager для изоляции от файловой системы.
 */
public class HttpTaskManagerPrioritizedTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        // очищаем все сущности
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    /**
     * Проверяет, что при отсутствии задач в порядке приоритета возвращается пустой список.
     */
    @Test
    public void testGetPrioritizedEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Для пустого списка должен возвращаться 200");
        Task[] tasks = HttpTaskServer.getGson().fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Ответ должен быть корректным JSON");
        assertEquals(0, tasks.length, "Список приоритетных задач должен быть пуст");
    }

    /**
     * Проверяет, что задачи возвращаются отсортированными по времени начала,
     * а задачи без времени начала не включаются в результат.
     */
    @Test
    public void testPrioritizedOrder() throws IOException, InterruptedException {
        // первая задача — самая ранняя
        Task first = new Task("Первая", "", TaskStatus.NEW);
        first.setStartTime(LocalDateTime.now().withSecond(0).withNano(0));
        first.setDuration(Duration.ofMinutes(30));
        manager.addTask(first);
        // вторая задача без startTime не должна попасть в приоритетный список
        Task noStart = new Task("Вторая", "", TaskStatus.NEW);
        manager.addTask(noStart);
        // третья задача — позже первой, но раньше четвёртой
        Task middle = new Task("Третья", "", TaskStatus.NEW);
        middle.setStartTime(first.getStartTime().get().plusMinutes(45));
        middle.setDuration(Duration.ofMinutes(15));
        manager.addTask(middle);
        // четвёртая задача — самая поздняя
        Task last = new Task("Четвертая", "", TaskStatus.NEW);
        last.setStartTime(first.getStartTime().get().plusHours(2));
        last.setDuration(Duration.ofMinutes(30));
        manager.addTask(last);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "При получении приоритетных задач должен возвращаться 200");
        Task[] tasks = HttpTaskServer.getGson().fromJson(response.body(), Task[].class);
        // список должен содержать только задачи со временем начала (3 штуки)
        assertEquals(3, tasks.length, "Задачи без startTime не должны попадать в список");
        // проверяем порядок
        assertEquals(first.getId(), tasks[0].getId(), "Первая задача должна быть самой ранней");
        assertEquals(middle.getId(), tasks[1].getId(), "Вторая задача должна иметь время между первой и последней");
        assertEquals(last.getId(), tasks[2].getId(), "Последняя задача должна быть самой поздней");
    }
}
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
 * Тесты для эндпоинта /tasks.
 * Проверяют корректность создания, чтения, обновления и удаления задач, а также обработку ошибок.
 */
public class HttpTaskManagerTasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        // Очищаем хранилище задач перед каждым тестом
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("AddTask", "d", TaskStatus.NEW);
        String json = HttpTaskServer.getGson().toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "При создании задачи должен возвращаться статус 201");
        // В менеджере должна появиться одна задача
        assertEquals(1, manager.getAllTasks().size());
        assertEquals("AddTask", manager.getAllTasks().getFirst().getTitle());
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        // Создаём две задачи непосредственно через менеджер
        Task t1 = new Task("t1", "d1", TaskStatus.NEW);
        Task t2 = new Task("t2", "d2", TaskStatus.NEW);
        manager.addTask(t1);
        manager.addTask(t2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        // Проверяем, что в ответе содержатся обе задачи
        Task[] tasks = HttpTaskServer.getGson().fromJson(response.body(), Task[].class);
        assertNotNull(tasks);
        assertEquals(2, tasks.length);
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("GetTaskById", "d", TaskStatus.NEW);
        manager.addTask(task);
        int id = task.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task fromResponse = HttpTaskServer.getGson().fromJson(response.body(), Task.class);
        assertEquals(task.getTitle(), fromResponse.getTitle());
        assertEquals(task.getDescription(), fromResponse.getDescription());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("UpdateTask", "d", TaskStatus.NEW);
        manager.addTask(task);
        // меняем статус
        task.setStatus(TaskStatus.DONE);
        String json = HttpTaskServer.getGson().toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        // проверяем, что обновление прошло
        assertEquals(TaskStatus.DONE, manager.getTaskById(task.getId()).orElseThrow().getStatus());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("DeleteTask", "Desc", TaskStatus.NEW);
        manager.addTask(task);
        int id = task.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty(), "После удаления список задач должен быть пустым");
    }

    @Test
    public void testGetTaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Должен возвращаться 404 для несуществующей задачи");
    }
}
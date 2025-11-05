package com.yandex.app.http;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
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
 * Тесты для эндпоинта /subtasks.
 * Проверяют создание, чтение, обновление, удаление подзадач, а также обработку конфликтов по времени.
 */
public class HttpTaskManagerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private final HttpClient client = HttpClient.newHttpClient();
    private Epic epic;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        // очищаем
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        // создаём эпик, к которому будут относиться подзадачи
        epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Subtask sub = new Subtask("Subtask", "Desc", TaskStatus.NEW, epic.getId());
        String json = HttpTaskServer.getGson().toJson(sub);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
        assertEquals(epic.getId(), manager.getAllSubtasks().getFirst().getEpicId());
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Subtask s1 = new Subtask("s1", "d1", TaskStatus.NEW, epic.getId());
        Subtask s2 = new Subtask("s2", "d2", TaskStatus.NEW, epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask[] subs = HttpTaskServer.getGson().fromJson(response.body(), Subtask[].class);
        assertNotNull(subs);
        assertEquals(2, subs.length);
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask sub = new Subtask("Single sub", "desc", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub);
        int id = sub.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask fromResp = HttpTaskServer.getGson().fromJson(response.body(), Subtask.class);
        assertEquals(sub.getEpicId(), fromResp.getEpicId());
        assertEquals(sub.getTitle(), fromResp.getTitle());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Subtask sub = new Subtask("To update", "desc", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub);
        sub.setStatus(TaskStatus.DONE);
        String json = HttpTaskServer.getGson().toJson(sub);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(TaskStatus.DONE, manager.getSubtaskById(sub.getId()).orElseThrow().getStatus());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Subtask sub = new Subtask("To delete", "d", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub);
        int id = sub.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
        // эпик после удаления подзадачи не должен содержать подзадач
        assertTrue(manager.getSubtasksOfEpic(epic.getId()).isEmpty());
    }

    @Test
    public void testOverlapSubtasks() throws IOException, InterruptedException {
        // Создаём первую подзадачу с указанием времени
        Subtask s1 = new Subtask("Overlap1", "d1", TaskStatus.NEW, epic.getId());
        s1.setStartTime(LocalDateTime.now().withSecond(0).withNano(0));
        s1.setDuration(Duration.ofMinutes(60));
        manager.addSubtask(s1);
        // Вторая подзадача пересекается по времени
        Subtask s2 = new Subtask("Overlap2", "d2", TaskStatus.NEW, epic.getId());
        s2.setStartTime(s1.getStartTime().get().plusMinutes(30));
        s2.setDuration(Duration.ofMinutes(30));
        String json = HttpTaskServer.getGson().toJson(s2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "При пересечении подзадачи должен возвращаться 406");
        // В менеджере всё ещё только одна подзадача
        assertEquals(1, manager.getAllSubtasks().size());
    }
}
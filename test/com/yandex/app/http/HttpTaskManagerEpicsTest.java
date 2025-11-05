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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для эндпоинта /epics.
 * Проверяют создание, чтение, получение подзадач эпика и удаление эпика.
 */
public class HttpTaskManagerEpicsTest {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("AddEpic", "");
        String json = HttpTaskServer.getGson().toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllEpics().size());
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic e1 = new Epic("e1", "d1");
        Epic e2 = new Epic("e2", "d2");
        manager.addEpic(e1);
        manager.addEpic(e2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic[] epics = HttpTaskServer.getGson().fromJson(response.body(), Epic[].class);
        assertNotNull(epics);
        assertEquals(2, epics.length);
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("GetEpicById", "");
        manager.addEpic(epic);
        int id = epic.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic fromResp = HttpTaskServer.getGson().fromJson(response.body(), Epic.class);
        assertEquals(epic.getTitle(), fromResp.getTitle());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("GetEpicSubtasks", "");
        manager.addEpic(epic);
        Subtask s1 = new Subtask("s1", "d", TaskStatus.NEW, epic.getId());
        Subtask s2 = new Subtask("s2", "d", TaskStatus.NEW, epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask[] subs = HttpTaskServer.getGson().fromJson(response.body(), Subtask[].class);
        assertNotNull(subs);
        assertEquals(2, subs.length);
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("DeleteEpic", "");
        manager.addEpic(epic);
        // добавляем подзадачу
        Subtask sub = new Subtask("sub", "", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub);
        int id = epic.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}
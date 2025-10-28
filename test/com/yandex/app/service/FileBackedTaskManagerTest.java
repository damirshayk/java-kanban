package com.yandex.app.service;

import com.yandex.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки сохранения и загрузки менеджера задач с поддержкой файлов.
 */
class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    /**
     * Создаёт новый FileBackedTaskManager с временным файлом перед каждым тестом.
     */
    @BeforeEach
    void setUp() throws IOException {
        // Создаём временный файл, который удалится после теста
        tempFile = File.createTempFile("java-kanban_test", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    /**
     * Тест на сохранение и загрузку пустого менеджера
     */
    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getAllTasks().isEmpty(), "Восстановленный менеджер должен быть пустым");
        assertTrue(loaded.getAllEpics().isEmpty(), "Эпики должны отсутствовать");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Подзадачи должны отсутствовать");
    }

    /**
     * Тест на сохранение и загрузку одной задачи
     */
    @Test
    void shouldSaveAndLoadSingleTask() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        manager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = loaded.getAllTasks();

        assertEquals(1, tasks.size(), "Должна загрузиться одна задача");
        assertEquals(task.getTitle(), tasks.getFirst().getTitle());
        assertEquals(task.getDescription(), tasks.getFirst().getDescription());
    }

    /**
     * Тест на сохранение и загрузку эпика с подзадачами
     */
    @Test
    void shouldSaveAndLoadEpicWithSubtasks() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Подзадача 1", "Описание 1", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("Подзадача 2", "Описание 2", TaskStatus.DONE, epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Epic> epics = loaded.getAllEpics();
        List<Subtask> subtasks = loaded.getAllSubtasks();

        assertEquals(1, epics.size(), "Должен быть загружен один эпик");
        assertEquals(2, subtasks.size(), "Должно быть загружено две подзадачи");

        Epic loadedEpic = epics.getFirst();
        assertEquals(epic.getTitle(), loadedEpic.getTitle(), "Название эпика должно совпадать");

        assertTrue(
                subtasks.stream().anyMatch(s -> s.getTitle().equals(sub1.getTitle())),
                "Подзадача 1 должна быть восстановлена"
        );
        assertTrue(
                subtasks.stream().anyMatch(s -> s.getTitle().equals(sub2.getTitle())),
                "Подзадача 2 должна быть восстановлена"
        );
    }

    /**
     * Тест на сохранение и загрузку повреждённого файла
     */
    @Test
    void shouldThrowWhenLoadingCorruptedFile() {
        // Портим файл вручную
        try {
            Files.writeString(tempFile.toPath(), "взял,и,испортил,весь,файл");
        } catch (IOException e) {
            fail("Не удалось записать повреждённый файл");
        }

        // Менеджер должен выбросить исключение при загрузке
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
    }

    /**
     * Тест на обработку ошибки записи в файл
     */
    @Test
    void shouldThrowManagerSaveExceptionWhenFileWriteFails() {
        // Создаём заведомо ошибочный путь
        File badFile = new File("/Exception/Exception.csv");
        FileBackedTaskManager badManager = new FileBackedTaskManager(badFile);

        Task task = new Task("Ошибка записи", "Тест IOException", TaskStatus.NEW);

        // Проверяем, что при попытке записи выбрасывается наша ManagerSaveException
        assertThrows(ManagerSaveException.class, () -> badManager.addTask(task),
                "При ошибке записи должен выбрасываться ManagerSaveException");
    }
}

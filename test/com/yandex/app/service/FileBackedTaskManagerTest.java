package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("java-kanban", ".csv");
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @AfterEach
    void cleanup() {
        if (tempFile != null) tempFile.delete();
    }

    @Test
    void shouldThrowWhenFileIsCorrupted() throws IOException {
        Files.writeString(tempFile.toPath(), "сломанная,строка");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
    }

    @Test
    void shouldNotThrowWhenSavingValidData() {
        assertDoesNotThrow(() -> manager.addTask(new Task("ОК", "Описание", TaskStatus.NEW)));
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tempFile));
    }

    // Тест на сохранение и загрузку истории
    @Test
    void shouldSaveAndLoadHistoryCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 11, 1, 10, 0);

        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        task.setStartTime(now);
        task.setDuration(Duration.ofMinutes(60));
        manager.addTask(task);

        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Подзадача", "Описание", TaskStatus.NEW, epic.getId());
        sub.setStartTime(now.plusHours(2));
        sub.setDuration(Duration.ofMinutes(30));
        manager.addSubtask(sub);

        // вызываем getById(), чтобы записать историю
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(sub.getId());

        // читаем содержимое файла и проверяем наличие строки истории
        String fileData;
        try {
            fileData = Files.readString(tempFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения временного файла", e);
        }
        assertTrue(fileData.contains("1,2,3"),
                "Файл должен содержать строку истории ID: 1,2,3");

        // загружаем менеджер заново
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        // история должна восстановиться корректно
        assertEquals(3, loaded.getHistory().size(),
                "После загрузки история должна содержать 3 записи");

        assertEquals(task.getId(), loaded.getHistory().get(0).getId());
        assertEquals(epic.getId(), loaded.getHistory().get(1).getId());
        assertEquals(sub.getId(), loaded.getHistory().get(2).getId());
    }
}

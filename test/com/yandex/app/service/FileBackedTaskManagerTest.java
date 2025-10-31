package com.yandex.app.service;

import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
}

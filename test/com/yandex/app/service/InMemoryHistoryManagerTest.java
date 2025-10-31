package com.yandex.app.service;

import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        assertTrue(historyManager.getHistory().isEmpty(), "История изначально должна быть пустой");
    }

    @Test
    void shouldRemoveFromStartMiddleEnd() {
        Task t1 = new Task("t1", "", TaskStatus.NEW);
        Task t2 = new Task("t2", "", TaskStatus.NEW);
        Task t3 = new Task("t3", "", TaskStatus.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(t1.getId());
        assertFalse(historyManager.getHistory().contains(t1));

        historyManager.remove(t2.getId());
        assertFalse(historyManager.getHistory().contains(t2));

        historyManager.remove(t3.getId());
        assertTrue(historyManager.getHistory().isEmpty());
    }
}

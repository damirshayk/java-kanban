package com.yandex.app.service;

import com.yandex.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Базовый абстрактный тест для всех реализаций TaskManager.
 * Проверяет общие требования.
 */
abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldAddAndFindTaskById() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        manager.addTask(task);

        Task found = manager.getTaskById(task.getId()).orElseThrow();
        assertEquals(task.getTitle(), found.getTitle());
        assertEquals(task.getDescription(), found.getDescription());
    }

    @Test
    void shouldAddEpicAndSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask sub = new Subtask("Подзадача", "Описание", TaskStatus.NEW, epic.getId());
        manager.addSubtask(sub);

        List<Subtask> subs = manager.getSubtasksOfEpic(epic.getId());
        assertEquals(1, subs.size(), "У эпика должна быть одна подзадача");
        assertEquals(epic.getId(), subs.getFirst().getEpicId());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        manager.addTask(task);

        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        Task updated = manager.getTaskById(task.getId()).orElseThrow();
        assertEquals(TaskStatus.DONE, updated.getStatus());
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = new Task("Удалить", "Описание", TaskStatus.NEW);
        manager.addTask(task);
        manager.deleteTaskById(task.getId());

        assertTrue(manager.getAllTasks().isEmpty(), "Задача должна быть удалена");
    }

    @Test
    void shouldCalculateEpicStatusCorrectly() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("s1", "Описание", TaskStatus.NEW, epic.getId());
        Subtask s2 = new Subtask("s2", "Описание", TaskStatus.NEW, epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).orElseThrow().getStatus());

        s1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(s1);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).orElseThrow().getStatus());

        s2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(s2);
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).orElseThrow().getStatus());

        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(s1);
        manager.updateSubtask(s2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldThrowWhenTasksOverlapInTime() {
        Task t1 = new Task("t1", "Описание", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.now());
        t1.setDuration(Duration.ofMinutes(60));
        manager.addTask(t1);

        // Берём значение из Optional
        LocalDateTime nextStart = t1.getStartTime().orElseThrow().plusMinutes(30);

        Task t2 = new Task("t2", "Описание", TaskStatus.NEW);
        t2.setStartTime(nextStart); // пересекается
        t2.setDuration(Duration.ofMinutes(30));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addTask(t2),
                "Должно выбрасывать исключение при пересечении временных интервалов");
    }

    @Test
    void historyShouldNotContainDuplicates() {
        Task t = new Task("История", "Описание", TaskStatus.NEW);
        manager.addTask(t);

        manager.getTaskById(t.getId());
        manager.getTaskById(t.getId());

        assertEquals(1, manager.getHistory().size(),
                "История не должна содержать дубликаты");
    }

    @Test
    void deletingTaskRemovesItFromHistory() {
        Task t = new Task("Удалить", "Описание", TaskStatus.NEW);
        manager.addTask(t);
        manager.getTaskById(t.getId());
        manager.deleteTaskById(t.getId());

        assertTrue(manager.getHistory().isEmpty(),
                "Удалённая задача должна исчезнуть из истории");
    }

    @Test
    void epicShouldNotContainDeletedSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask s = new Subtask("Подзадача", "Описание", TaskStatus.NEW, epic.getId());
        manager.addSubtask(s);

        manager.deleteSubtaskById(s.getId());

        assertTrue(manager.getSubtasksOfEpic(epic.getId()).isEmpty(),
                "После удаления подзадачи у эпика не должно быть подзадач");
    }

    @Test
    void shouldReturnPrioritizedTasksInOrder() {
        Task t1 = new Task("t1", "Описание", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.now());
        t1.setDuration(Duration.ofMinutes(15));

        Task t2 = new Task("t2", "Описание", TaskStatus.NEW);
        t2.setStartTime(t1.getStartTime().orElseThrow().plusHours(2));
        t2.setDuration(Duration.ofMinutes(15));

        manager.addTask(t1);
        manager.addTask(t2);

        List<Task> sorted = manager.getPrioritizedTasks();
        assertEquals(List.of(t1, t2), sorted, "Задачи должны быть отсортированы по startTime");
    }
}

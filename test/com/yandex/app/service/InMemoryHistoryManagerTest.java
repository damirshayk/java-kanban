package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для InMemoryHistoryManager и проверки истории в связке с InMemoryTaskManager.
 */
class InMemoryHistoryManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    /** Проверяет добавление в историю.
     */
    @Test
    void historyShouldAddTask() {
        Task task = new Task("Тест", "Описание", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task.getId(), history.getFirst().getId(), "В истории должен быть правильный id задачи");
    }

    /** Проверяет, что история не содержит дубликатов.
     */
    @Test
    void historyDoesNotStoreDuplicates() {
        Task task = new Task("Test", "Desc", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликаты");
    }

    /** Проверяет удаление задачи из истории при её удалении из менеджера.
     */
    @Test
    void deletingTaskRemovesItFromHistory() {
        Task task = new Task("Удаляемая", "Описание", TaskStatus.NEW);
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());
        assertTrue(taskManager.getHistory().contains(task),
                "История должна содержать задачу после просмотра");

        taskManager.deleteTaskById(task.getId());
        assertFalse(taskManager.getHistory().contains(task),
                "История не должна содержать удалённую задачу");
    }

    /** Проверяет, что при удалении эпика из менеджера, его подзадачи также удаляются из истории.
     */
    @Test
    void deletingEpicAlsoRemovesItsSubtasksFromHistory() {
        Epic epic = new Epic("Epic", "0");
        taskManager.addEpic(epic);

        Subtask sub1 = new Subtask("sub1", "desc", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("sub2", "desc", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(sub1);
        taskManager.addSubtask(sub2);

        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(sub1.getId());
        taskManager.getSubtaskById(sub2.getId());

        taskManager.deleteEpicById(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(epic), "Эпик должен удаляться из истории");
        assertFalse(history.contains(sub1), "Подзадачи эпика тоже должны удаляться из истории");
        assertFalse(history.contains(sub2), "Подзадачи эпика тоже должны удаляться из истории");
    }

    /** Проверяет изменение статуса эпика в зависимости от статусов его подзадач.
     */
    @Test
    void epicStatusChangesBasedOnSubtasks() {
        Epic epic = new Epic("Epic", "Status");
        taskManager.addEpic(epic);

        Subtask sub1 = new Subtask("sub1", "0", TaskStatus.NEW, epic.getId());
        Subtask sub2 = new Subtask("sub2", "0", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(sub1);
        taskManager.addSubtask(sub2);

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic.getId()).getStatus(),
                "Если все подзадачи новые, эпик должен быть NEW");

        sub1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(sub1);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(),
                "Смешанные статусы должны приводить к IN_PROGRESS");

        sub2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(sub2);
        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus(),
                "Если все подзадачи DONE, эпик должен быть DONE");
    }

    /** Проверяет, что задачи, хранящиеся в менеджере, являются неизменяемыми извне.
     */
    @Test
    void tasksInManagerAreImmutable() {
        Task task = new Task("1", "0", TaskStatus.NEW);
        taskManager.addTask(task);

        task.setTitle("Меняем");
        task.setStatus(TaskStatus.DONE);

        Task retrieved = taskManager.getAllTasks().getFirst();

        assertEquals("1", retrieved.getTitle(),
                "Название в менеджере не должно измениться");
        assertEquals(TaskStatus.NEW, retrieved.getStatus(),
                "Статус в менеджере не должен измениться");
    }

    /** Проверяет, что у эпика нет подзадач после удаления всех его подзадач.
     */
    @Test
    void epicShouldNotContainDeletedSubtaskIds() {
        Epic epic = new Epic("Epic", "0");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("sub", "0", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.deleteSubtaskById(subtask.getId());

        assertTrue(taskManager.getSubtasksOfEpic(epic.getId()).isEmpty(),
                "После удаления подзадачи список подзадач у эпика должен быть пустым");
    }

}

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
 * Тесты для InMemoryTaskManager, Epic, Subtask, HistoryManager и Managers.
 */
class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void tasksWithSameIdAreEqual() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.DONE);
        task2.setId(1);

        // проверьте, что экземпляры класса Task равны друг другу, если равен их id;
        assertEquals(task1, task2, "Задачи с одинаковыми id должны быть равны");
    }

    @Test
    void subtasksWithSameIdAreEqual() {
        Subtask sub1 = new Subtask("Подзадача 1", "Описание 1", TaskStatus.NEW, 3);
        sub1.setId(1);
        Subtask sub2 = new Subtask("Подзадача 2", "Описание 2", TaskStatus.DONE, 4);
        sub2.setId(1);

        // проверьте, что наследники класса Task равны друг другу, если равен их id;
        assertEquals(sub1, sub2, "Подзадачи с одинаковыми id должны быть равны");
    }

    @Test
    void epicsWithSameIdAreEqual() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        epic1.setId(1);
        Epic epic2 = new Epic("Эпик 2", "Описание 1");
        epic2.setId(1);

        // проверьте, что наследники класса Task равны друг другу, если равен их id;
        assertEquals(epic1, epic2, "Эпики с одинаковыми id должны быть равны");
    }

    @Test
    void epicCannotContainItselfAsSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(1);

        // проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
        assertThrows(IllegalArgumentException.class, () -> epic.addSubtaskId(1));
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", TaskStatus.NEW, 1);
        subtask.setId(1);

        // проверьте, что объект Subtask нельзя сделать своим же эпиком;
        assertThrows(IllegalArgumentException.class, () -> taskManager.addSubtask(subtask));
    }

    @Test
    void managersReturnInitializedInstances() {
        /* убедитесь, что утилитарный класс всегда возвращает проинициализированные
        и готовые к работе экземпляры менеджеров; */
        assertNotNull(Managers.getDefault(), "TaskManager должен возвращаться и быть инициализированным");
        assertNotNull(Managers.getDefaultHistory(),
                "HistoryManager должен возвращаться и быть инициализированным");
    }

    @Test
    void addAndFindTasksById() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());
        List<Task> history = taskManager.getHistory();

        // проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
        assertTrue(history.contains(task), "Задача должна быть в истории после вызова getTaskById");
    }

    @Test
    void noIdConflictsBetweenGivenAndGeneratedIds() {
        Task givenTask = new Task("ID", "Описание", TaskStatus.NEW);
        givenTask.setId(100);
        taskManager.addTask(givenTask);

        Task generatedTask = new Task("ID2", "Описание", TaskStatus.NEW);
        taskManager.addTask(generatedTask);

        // проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
        assertNotEquals(givenTask.getId(), generatedTask.getId(),
                "Сгенерированный id не должен конфликтовать с заданным");
    }

    @Test
    void taskDataImmutabilityAfterAddingToManager() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        taskManager.addTask(task);

        task.setTitle("Название задачи");
        task.setDescription("Описание задачи");
        task.setStatus(TaskStatus.DONE);

        Task retrieved = taskManager.getAllTasks().get(0);

        // создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
        assertNotEquals("Название задачи", retrieved.getTitle(),
                "Название задачи в менеджере не должно измениться");
        assertNotEquals("Описание задачи", retrieved.getDescription(),
                "Описание задачи в менеджере не должно измениться");
        assertNotEquals(TaskStatus.DONE, retrieved.getStatus(),
                "Статус задачи в менеджере не должен измениться");
    }

    @Test
    void historyManagerStoresPreviousVersionsOfTask() {
        Task task = new Task("История", "Описание", TaskStatus.NEW);
        task.setId(50);
        historyManager.add(task);

        task.setTitle("Изменил наименование");
        List<Task> history = historyManager.getHistory();
        Task fromHistory = history.get(0);

        // убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
        assertNotEquals("Изменил наименование", fromHistory.getTitle(),
                "HistoryManager должен хранить предыдущую версию задачи");
    }

    @Test
    void deletingTaskRemovesItFromHistory() {
        Task task = new Task("Удаляемая", "Описание", TaskStatus.NEW);
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());

        taskManager.deleteTaskById(task.getId());
        List<Task> history = taskManager.getHistory();

        // проверка удаления задачи из истории просмотров, при её уделении из менеджера
        assertFalse(history.contains(task), "История не должна содержать удалённую задачу");
    }
}

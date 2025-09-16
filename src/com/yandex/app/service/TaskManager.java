package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.List;

/**
 * Интерфейс TaskManager определяет методы для управления задачами, эпиками и подзадачами.
 */
public interface TaskManager {

    // Добавляет задачи, эпики и подзадачи
    void addTask(Task task);

    // При добавлении эпика, его статус всегда должен быть "NEW"
    void addEpic(Epic epic);

    // При добавлении подзадачи, её id эпика должен быть валидным
    void addSubtask(Subtask subtask);

    // Возвращает списки всех задач, эпиков и подзадач
    List<Task> getAllTasks();

    // Возвращает список всех эпиков (вместе с их подзадачами)
    List<Epic> getAllEpics();

    // Возвращает список всех подзадач
    List<Subtask> getAllSubtasks();

    // Возвращает задачи по id
    Task getTaskById(int id);

    // Возвращает эпики по id (вместе с их подзадачами)
    Epic getEpicById(int id);

    // Возвращает подзадачи по id
    Subtask getSubtaskById(int id);

    // При обновлении задачи, статус задачи меняется на переданный
    void updateTask(Task task);

    // При обновлении эпика, статус эпика не меняется
    void updateEpic(Epic epic);

    // При обновлении подзадачи, статус эпика должен пересчитываться
    void updateSubtask(Subtask subtask);

    // При удалении задачи, её id также удаляется из истории просмотров
    void deleteTaskById(int id);

    // При удалении эпика, все его подзадачи также удаляются
    void deleteEpicById(int id);

    // При удалении подзадачи, её id также удаляется из эпика
    void deleteSubtaskById(int id);

    // Удаляет все задачи, эпики и подзадачи
    void deleteAllTasks();

    // При удалении всех эпиков, все их подзадачи также удаляются
    void deleteAllEpics();

    // При удалении всех подзадач, статусы всех эпиков должны стать "NEW"
    void deleteAllSubtasks();

    // Возвращает список всех подзадач определённого эпика
    List<Subtask> getSubtasksOfEpic(int epicId);

    // Возвращает список всех задач, упорядоченных по времени начала (начиная с самых ранних)
    List<Task> getHistory();
}

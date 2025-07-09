package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.List;

/**
 * Интерфейс TaskManager определяет методы для управления задачами, эпиками и подзадачами.
 */
public interface TaskManager {

    void addTask(Task task);
    void addEpic(Epic epic);
    void addSubtask(Subtask subtask);

    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();

    Task getTaskById(int id);
    Epic getEpicById(int id);
    Subtask getSubtaskById(int id);

    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);

    void deleteTaskById(int id);
    void deleteEpicById(int id);
    void deleteSubtaskById(int id);

    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory(); // Метод истории просмотров (в техзадании)
}

package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.*;

/**
 * Класс InMemoryTaskManager реализует интерфейс TaskManager.
 * Отвечает за хранение и управление задачами, эпиками и подзадачами.
 * Использует HistoryManager для хранения истории просмотров задач.
 */
public class InMemoryTaskManager implements TaskManager {

    private int nextId = 1; // Счётчик для генерации уникальных id задач

    /**
     * Хранилища задач по типам
     */
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    /**
     * Менеджер истории просмотров
     */
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    /**
     * Генерирует уникальный id для новой задачи.
     */
    private int generateId() {
        return nextId++;
    }

    /**
     * Добавляет новую Task в менеджер.
     */
    @Override
    public void addTask(Task task) {
        int id = task.getId() > 0 ? task.getId() : generateId();
        if (task.getId() >= nextId) {
            nextId = task.getId() + 1;
        }

        // создаём копию задачи
        Task copy = new Task(task);
        copy.setId(id);

        tasks.put(id, copy);
        task.setId(id); // Обновляем id у исходного объекта
    }

    /**
     * Добавляет новый Epic в менеджер.
     */
    @Override
    public void addEpic(Epic epic) {
        int id = epic.getId() > 0 ? epic.getId() : generateId();
        if (epic.getId() >= nextId) {
            nextId = epic.getId() + 1;
        }

        // создаём копию эпика
        Epic copy = new Epic(epic);
        copy.setId(id);

        epics.put(id, copy);
        epic.setId(id); // Обновляем id у исходного объекта
    }

    /**
     * Добавляет новую Subtask в менеджер.
     * Также обновляет соответствующий Epic, добавляя id подзадачи и пересчитывая его статус.
     */
    @Override
    public void addSubtask(Subtask subtask) {
        int id = subtask.getId() > 0 ? subtask.getId() : generateId();
        if (subtask.getId() >= nextId) {
            nextId = subtask.getId() + 1;
        }

        Subtask copy = new Subtask(subtask);
        copy.setId(id);

        if (subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Подзадача не может быть своим же эпиком");
        }

        subtasks.put(id, copy);
        subtask.setId(id); //Обновляем id у исходного объекта

        Epic epic = epics.get(copy.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    /**
     * Возвращает список всех задач Task.
     */
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Возвращает список всех эпиков Epic.
     */
    @Override
    public List<Epic> getAllEpics() {
        return List.copyOf(epics.values());
    }

    /**
     * Возвращает список всех подзадач Subtask.
     */
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /**
     * Возвращает задачу по id. Добавляет её в историю просмотров.
     */
    @Override
    public void getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
    }

    /**
     * Возвращает эпик по id. Добавляет его в историю просмотров.
     */
    @Override
    public void getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
    }

    /**
     * Возвращает подзадачу по id. Добавляет её в историю просмотров.
     */
    @Override
    public void getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
    }

    /**
     * Обновляет существующую задачу.
     */
    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    /**
     * Обновляет существующий эпик и его статус.
     */
    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    /**
     * Обновляет существующую подзадачу и статус соответствующего эпика.
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    /**
     * Удаляет задачу по id.
     */
    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id); // Удаляем задачу
        historyManager.remove(id); // Удаляем задачу из истории просмотров
    }

    /**
     * Удаляет эпик по id, а также все его подзадачи.
     */
    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    /**
     * Удаляет подзадачу по id и обновляет статус соответствующего эпика.
     */
    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    /**
     * Удаляет все задачи.
     */
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    /**
     * Удаляет все эпики и связанные с ними подзадачи.
     */
    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    /**
     * Удаляет все подзадачи и очищает их у эпиков.
     */
    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    /**
     * Возвращает список подзадач, принадлежащих конкретному эпику.
     */
    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        List<Subtask> result = new ArrayList<>();
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                result.add(subtasks.get(subtaskId));
            }
        }
        return result;
    }

    /**
     * Возвращает историю последних 10 просмотренных задач.
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /**
     * Обновление статусов Epic.
     *<p> Если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть - NEW.
     *<p> Если все подзадачи имеют статус DONE, то и эпик считается завершённым — DONE.
     *<p> Во всех остальных случаях статус должен быть - IN_PROGRESS.
     */
    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subtaskIds) {
            TaskStatus status = subtasks.get(id).getStatus();
            if (status != TaskStatus.NEW) allNew = false;
            if (status != TaskStatus.DONE) allDone = false;
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}

package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Менеджер истории просмотров задач
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
     *
     * @param task задача для добавления
     * @throws IllegalArgumentException если task == null
     */
    @Override
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("[" + getClass().getName() + ".addTask] не может быть null.");
        }
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
     *
     * @param epic эпик для добавления
     * @throws IllegalArgumentException если epic == null
     */
    @Override
    public void addEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("[" + getClass().getName() + ".addEpic] не может быть null.");
        }
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
     *
     * @param subtask подзадача для добавления
     * @throws IllegalArgumentException если подзадача равна null
     * @throws IllegalArgumentException если эпик с указанным id не найден
     * @throws IllegalArgumentException если подзадача ссылается на саму себя как на эпик
     */
    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("[" + getClass().getName() + ".addSubtask] не может быть null.");
        }

        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            throw new IllegalArgumentException("Epic с id " + subtask.getEpicId() + " не найден.");
        }

        int id = subtask.getId() > 0 ? subtask.getId() : generateId();
        if (subtask.getId() >= nextId) {
            nextId = subtask.getId() + 1;
        }

        Subtask copy = new Subtask(subtask);
        copy.setId(id);
        subtasks.put(id, copy);
        subtask.setId(id); //Обновляем id у исходного объекта

        epic = epics.get(copy.getEpicId());
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
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return new Task(task); // возвращаем копию
        }
        return null;
    }

    /**
     * Возвращает эпик по id. Добавляет его в историю просмотров.
     */
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return new Epic(epic); // возвращаем копию
        }
        return null;
    }

    /**
     * Возвращает подзадачу по id. Добавляет её в историю просмотров.
     */
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return new Subtask(subtask); // возвращаем копию
        }
        return null;
    }

    /**
     * Обновляет существующую задачу.
     *
     * @param task задача для обновления
     * @throws IllegalArgumentException если task == null
     * @throws IllegalArgumentException если задача с указанным id не существует
     */
    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("[" + getClass().getName() + ".updateTask] не может быть null.");
        }
        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с id " + task.getId() + " не существует.");
        }
        tasks.put(task.getId(), new Task(task)); // Клонируем объект
    }

    /**
     * Обновляет существующий эпик и его статус.
     *
     * @param epic эпик для обновления
     * @throws IllegalArgumentException если epic == null
     * @throws IllegalArgumentException если эпик с указанным id не существует
     */
    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("[" + getClass().getName() + ".updateEpic] не может быть null.");
        }
        if (!epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Эпик с id " + epic.getId() + " не существует.");
        }
        epics.put(epic.getId(), new Epic(epic)); // Клонируем объект
        updateEpicStatus(epic);
    }

    /**
     * Обновляет существующую подзадачу и статус соответствующего эпика.
     *
     * @param subtask подзадача для обновления
     * @throws IllegalArgumentException если subtask == null
     * @throws IllegalArgumentException если подзадача с указанным id не существует
     * @throws IllegalArgumentException если эпик, к которому принадлежит подзадача, не найден
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("[" + getClass().getName() + ".updateSubtask] не может быть null.");
        }
        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с id " + subtask.getId() + " не существует.");
        }
        subtasks.put(subtask.getId(), new Subtask(subtask)); // Клонируем объект
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        } else {
            throw new IllegalArgumentException(
                    "Эпик с id " + subtask.getEpicId() + " не найден для подзадачи с id " + subtask.getId());
        }
    }

    /**
     * Удаляет задачу по id.
     *
     * @param id идентификатор задачи
     * @throws IllegalArgumentException если задача с указанным id не найдена
     */
    @Override
    public void deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException("Задача с id " + id + " не найдена.");
        }
        tasks.remove(id); // Удаляем задачу
        historyManager.remove(id); // Удаляем задачу из истории просмотров
    }

    /**
     * Удаляет эпик по id, а также все его подзадачи.
     *
     * @param id идентификатор эпика
     * @throws IllegalArgumentException если эпик с указанным id не найден
     */
    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            throw new IllegalArgumentException("Эпик с id " + id + " не найден.");
        }
        Epic epic = epics.remove(id);
        // Удаляем подзадачи эпика из памяти и истории
        for (int subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        historyManager.remove(id); // Удаляем эпик из истории
    }

    /**
     * Удаляет подзадачу по id и обновляет статус соответствующего эпика.
     *
     * @param id идентификатор подзадачи
     * @throws IllegalArgumentException если подзадача с указанным id не найдена
     */
    @Override
    public void deleteSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Подзадача с id " + id + " не найдена.");
        }
        Subtask subtask = subtasks.remove(id);
        historyManager.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    /**
     * Удаляет все задачи.
     * Также очищает историю просмотров.
     */
    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    /**
     * Удаляет все эпики и связанные с ними подзадачи.
     * Также очищает их из истории просмотров.
     */
    @Override
    public void deleteAllEpics() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        epics.clear();
        subtasks.clear();
    }

    /**
     * Удаляет все подзадачи и очищает их у эпиков.
     * Также очищает их из истории просмотров.
     */
    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }

        subtasks.clear();
    }

    /**
     * Возвращает список подзадач, принадлежащих конкретному эпику.
     *
     * @param epicId идентификатор эпика
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
     * Возвращает список последних просмотренных задач.
     *
     * @return список задач
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /**
     * Обновление статусов Epic.
     * <p> Если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть - NEW.
     * <p> Если все подзадачи имеют статус DONE, то и эпик считается завершённым — DONE.
     * <p> Во всех остальных случаях статус должен быть - IN_PROGRESS.
     *
     * @param epic эпик, статус которого нужно обновить
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
            Subtask subtask = subtasks.get(id);
            if (subtask == null) {
                continue; // если подзадача удалена из памяти, пропускаем её
            }
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

package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс InMemoryTaskManager реализует интерфейс TaskManager.
 * Отвечает за хранение и управление задачами, эпиками и подзадачами.
 * Использует HistoryManager для хранения истории просмотров задач.
 */
public class InMemoryTaskManager implements TaskManager {

    private int nextId = 1; // Счётчик для генерации уникальных id задач

    private static final Duration SLOT_SIZE = Duration.ofMinutes(15);
    private static final Duration MAX_PLANNING = Duration.ofDays(365);

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<LocalDateTime, Boolean> timeSlots = new HashMap<>();

    /**
     * Конструктор инициализирует менеджер задач и временную сетку.
     */
    public InMemoryTaskManager() {
        initializeTimeGrid();
    }

    /**
     * Инициализирует временную сетку для отслеживания занятых слотов.
     */
    private void initializeTimeGrid() {
        LocalDateTime now = LocalDateTime.now()
                .minusDays(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime end = now.plus(MAX_PLANNING);

        for (LocalDateTime t = now; t.isBefore(end); t = t.plus(SLOT_SIZE)) {
            timeSlots.put(t, false);
        }
    }

    /**
     * Менеджер истории просмотров задач
     */
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    /**
     * Хранилище задач в порядке приоритета (по времени начала)
     * Используется TreeSet для автоматической сортировки.
     * Задачи без startTime не включаются в это множество.
     */
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(
                            (Task t) -> t.getStartTime().orElse(LocalDateTime.MAX),
                            Comparator.naturalOrder()
                    )
                    .thenComparing(Task::getId)
    );

    /**
     * Генерирует уникальный id для новой задачи.
     */
    private int generateId() {
        return nextId++;
    }

    // Общий метод для присвоения ID
    private int assignId(Task task) {
        if (task.getId() <= 0) return generateId();
        nextId = Math.max(nextId, task.getId() + 1);
        return task.getId();
    }

    // Добавление в TreeSet только если есть startTime
    private void addToPrioritized(Task task) {
        task.getStartTime().ifPresent(t -> prioritizedTasks.add(task));
    }


    /**
     * Добавляет новую Task в менеджер.
     *
     * @param task задача для добавления
     * @throws IllegalArgumentException если task == null
     */
    @Override
    public void addTask(Task task) {
        Objects.requireNonNull(task, "Task не может быть null");
        if (hasOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени: " + task.getTitle());
        }
        occupySlots(task);

        int id = assignId(task);
        Task copy = new Task(task);
        copy.setId(id);
        tasks.put(id, copy);
        addToPrioritized(copy); // Добавляем только с валидным временем
        task.setId(id);
    }

    /**
     * Добавляет новый Epic в менеджер.
     *
     * @param epic эпик для добавления
     * @throws IllegalArgumentException если epic == null
     */
    @Override
    public void addEpic(Epic epic) {
        Objects.requireNonNull(epic, "Epic не может быть null");
        int id = assignId(epic);
        Epic copy = new Epic(epic);
        copy.setId(id);
        epics.put(id, copy);
        epic.setId(id);
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
        Objects.requireNonNull(subtask, "Subtask не может быть null");
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Epic с id " + subtask.getEpicId() + " не найден.");
        }

        if (subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Подзадача не может ссылаться на саму себя как на эпик.");
        }

        if (hasOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени: " + subtask.getTitle());
        }
        occupySlots(subtask);

        int id = assignId(subtask);
        Subtask copy = new Subtask(subtask);
        copy.setId(id);
        subtasks.put(id, copy);
        addToPrioritized(copy);
        subtask.setId(id);

        epic.addSubtaskId(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);
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
     * @return Optional<Task> вместо null
     */
    @Override
    public Optional<Task> getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return Optional.of(new Task(task));
        }
        return Optional.empty();
    }

    /**
     * Возвращает эпик по id. Добавляет его в историю просмотров.
     * @return Optional<Epic> вместо null
     */
    @Override
    public Optional<Epic> getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return Optional.of(new Epic(epic));
        }
        return Optional.empty();
    }

    /**
     * Возвращает подзадачу по id. Добавляет её в историю просмотров.
     * @return Optional<Subtask> вместо null
     */
    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return Optional.of(new Subtask(subtask));
        }
        return Optional.empty();
    }

    /**
     * Обновляет существующую задачу.
     *
     * @param task задача для обновления
     * @throws IllegalArgumentException если task == null
     * @throws IllegalArgumentException если задача с указанным id не найдена
     * @throws IllegalArgumentException если задача пересекается по времени
     */
    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Task не может быть null");
        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с id " + task.getId() + " не найдена.");
        }

        Task oldTask = tasks.get(task.getId());
        prioritizedTasks.remove(oldTask);
        releaseSlots(oldTask); // Добавляем: освобождаем старые слоты

        if (hasOverlap(task)) {
            occupySlots(oldTask); // возвращаем старые слоты, если новая версия не подходит
            throw new IllegalArgumentException("Задача пересекается по времени: " + task.getTitle());
        }

        occupySlots(task); // теперь заносим новые интервалы
        tasks.put(task.getId(), new Task(task));
        addToPrioritized(task);
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
        Objects.requireNonNull(epic);
        if (!epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Эпик с id " + epic.getId() + " не найден.");
        }
        epics.put(epic.getId(), new Epic(epic)); // Клонируем объект
        updateEpicStatus(epic);
        updateEpicTime(epic); // Обновляем время эпика
    }

    /**
     * Обновляет существующую подзадачу и соответствующий эпик.
     *
     * @param subtask подзадача для обновления
     * @throws IllegalArgumentException если subtask == null
     * @throws IllegalArgumentException если подзадача с указанным id не найдена
     * @throws IllegalArgumentException если подзадача пересекается по времени
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask);
        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с id " + subtask.getId() + " не найдена.");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());
        prioritizedTasks.remove(oldSubtask);
        releaseSlots(oldSubtask); // освобождаем старые слоты

        if (hasOverlap(subtask)) {
            occupySlots(oldSubtask); // возвращаем старые, если новая пересекается
            throw new IllegalArgumentException("Подзадача пересекается по времени: " + subtask.getTitle());
        }

        occupySlots(subtask);
        subtasks.put(subtask.getId(), new Subtask(subtask));
        addToPrioritized(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            updateEpicTime(epic);
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
        Task removed = tasks.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("Задача с id " + id + " не найдена.");
        }
        releaseSlots(removed); // Освобождаем временные интервалы
        prioritizedTasks.remove(removed); // Удаляем из приоритетов
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
        Epic epic = epics.remove(id);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + id + " не найден.");
        }
        // Удаляем все связанные подзадачи
        for (int subId : epic.getSubtaskIds()) {
            prioritizedTasks.remove(subtasks.remove(subId));
            historyManager.remove(subId);
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
        Subtask removed = subtasks.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("Подзадача с id " + id + " не найдена.");
        }
        releaseSlots(removed); // Освобождаем временные интервалы
        prioritizedTasks.remove(removed);// Удаляем из приоритетов
        historyManager.remove(id); // Удаляем из истории просмотров

        Epic epic = epics.get(removed.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    /**
     * Удаляет все задачи.
     * Также очищает историю просмотров.
     */
    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(t -> {
            releaseSlots(t); // Освобождаем временные интервалы
            historyManager.remove(t.getId());
        });
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    /**
     * Удаляет все эпики и связанные с ними подзадачи.
     * Также очищает их из истории просмотров.
     */
    @Override
    public void deleteAllEpics() {
        epics.values().forEach(e -> historyManager.remove(e.getId()));
        subtasks.values().forEach(s -> historyManager.remove(s.getId()));
        prioritizedTasks.removeAll(subtasks.values());
        prioritizedTasks.removeAll(epics.values());
        epics.clear();
        subtasks.clear();
    }

    /**
     * Удаляет все подзадачи и очищает их у эпиков.
     * Также очищает их из истории просмотров.
     */
    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(s -> {
            releaseSlots(s); // Освобождаем временные интервалы
            historyManager.remove(s.getId());
        });
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();

        epics.values().forEach(e -> {
            e.clearSubtasks();
            updateEpicStatus(e);
            updateEpicTime(e);
        });
    }

    /**
     * Возвращает список подзадач, принадлежащих конкретному эпику.
     *
     * @param epicId идентификатор эпика
     * @return список подзадач
     */
    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        // Заменил на Stream API
        return epics.containsKey(epicId)
                ? epics.get(epicId).getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList()
                : List.of();
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

    /** Возвращает список задач в порядке приоритета (по времени начала).
     * Задачи без startTime не включаются в этот список.
     */
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    /**
     * Проверяет пересечение двух задач по принципу наложения отрезков.
     * Возвращает true, если интервалы выполнения задач пересекаются.
     */
    private boolean isOverlapping(Task a, Task b) {
        if (a.getStartTime().isEmpty() || a.getDuration().isEmpty()
                || b.getStartTime().isEmpty() || b.getDuration().isEmpty()) {
            return false;
        }

        LocalDateTime aStart = a.getStartTime().get();
        LocalDateTime aEnd = aStart.plus(a.getDuration().get());
        LocalDateTime bStart = b.getStartTime().get();
        LocalDateTime bEnd = bStart.plus(b.getDuration().get());

        // Математический метод наложения отрезков:
        // [aStart, aEnd) и [bStart, bEnd) пересекаются, если начало одной < конца другой и наоборот
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    /**
     * Проверяет пересечение задачи с уже занятыми интервалами.
     */
    private boolean hasOverlap(Task task) {
        if (task.getStartTime().isEmpty() || task.getDuration().isEmpty()) {
            return false;
        }

        return getPrioritizedTasks().stream()
                .filter(existing -> existing.getId() != task.getId()) // не сравниваем саму с собой
                .anyMatch(existing -> isOverlapping(task, existing));
    }

    /**
     * Помечает интервалы задачи как занятые.
     */
    private void occupySlots(Task task) {
        if (task.getStartTime().isEmpty() || task.getDuration().isEmpty()) {
            return;
        }

        LocalDateTime start = task.getStartTime().get();
        LocalDateTime end = task.getEndTime().orElse(start);

        for (LocalDateTime slot = start;
             slot.isBefore(end);
             slot = slot.plus(SLOT_SIZE)) {
            timeSlots.put(slot, true);
        }
    }

    /**
     * Освобождает интервалы задачи.
     */
    private void releaseSlots(Task task) {
        if (task.getStartTime().isEmpty() || task.getDuration().isEmpty()) return;

        LocalDateTime start = task.getStartTime().get();
        LocalDateTime end = task.getEndTime().orElse(start);

        for (LocalDateTime slot = start;
             slot.isBefore(end);
             slot = slot.plus(SLOT_SIZE)) {
            timeSlots.put(slot, false);
        }
    }

    // Пересчёт времени у Epic
    private void updateEpicTime(Epic epic) {
        List<Subtask> subs = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
        epic.updateEpicTime(subs);
    }

    // Пересчёт статуса у Epic
    private void updateEpicStatus(Epic epic) {
        List<Integer> ids = epic.getSubtaskIds();
        if (ids.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;

        for (int id : ids) {
            Subtask s = subtasks.get(id);
            if (s == null) continue;
            if (s.getStatus() != TaskStatus.NEW) allNew = false;
            if (s.getStatus() != TaskStatus.DONE) allDone = false;
        }

        if (allDone) epic.setStatus(TaskStatus.DONE);
        else if (allNew) epic.setStatus(TaskStatus.NEW);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }
}

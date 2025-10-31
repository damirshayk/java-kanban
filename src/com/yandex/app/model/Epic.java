package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Класс Epic расширяет Task и хранит список id подзадач.
 * Используется для группировки подзадач в рамках одной большой задачи.
 */
public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    private LocalDateTime endTime; // время завершения эпика (самое позднее из подзадач)

    /**
     * Конструктор для создания нового эпика.
     *
     * @param title       название эпика
     * @param description описание эпика
     */
    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW);
    }

    /**
     * Конструктор копирования для создания нового эпика на основе существующего.
     *
     * @param other существующий эпик для копирования
     */
    public Epic(Epic other) {
        super(other.getTitle(), other.getDescription(), other.getStatus());
        this.setId(other.getId());
        this.subtaskIds = new ArrayList<>(other.getSubtaskIds());
        this.duration = other.duration;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }

    /**
     * Получает список всех подзадач (возвращает копию).
     *
     * @return список id подзадач эпика
     */
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    @Override
    public TypeTask getType() {
        return TypeTask.EPIC;
    }

    /**
     * Добавляет id в список подзадач эпика.
     *
     * @throws IllegalArgumentException если переданный id совпадает с id самого эпика
     */
    public void addSubtaskId(int id) {
        if (id == this.getId()) {
            throw new IllegalArgumentException("Эпик не может содержать сам себя как подзадачу");
        }
        subtaskIds.add(id);
    }

    /**
     * Удаляет id из списка подзадач эпика.
     *
     * @param id id подзадачи для удаления
     */
    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    /**
     * Очищает список подзадач эпика.
     * После вызова этого метода эпик не будет содержать никаких подзадач.
     */
    public void clearSubtasks() {
        subtaskIds.clear();
    }

    /**
     * Обновляет вычисляемые поля (duration, startTime, endTime)
     * на основе переданных подзадач.
     *
     * @param subtasks список всех подзадач эпика
     */
    public void updateEpicTime(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.duration = Duration.ZERO;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        // Рассчитываем общую продолжительность
        this.duration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Duration.ZERO, Duration::plus);

        // Находим самое раннее время начала
        this.startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder())
                .orElse(null);

        // Находим самое позднее время окончания
        this.endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * Возвращает время окончания эпика.
     *
     * @return Optional<LocalDateTime> — конец последней подзадачи
     */
    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    /**
     * Возвращает строковое представление эпика, включая его подзадачи.
     *
     * @return строковое представление эпика
     */
    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIds=" + subtaskIds +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                '}';
    }

    /**
     * Создает и возвращает копию текущего объекта Epic.
     *
     * @return новая копия Epic
     */
    @Override
    public Epic clone() {
        return new Epic(this);
    }
}

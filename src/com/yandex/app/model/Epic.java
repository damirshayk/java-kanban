package com.yandex.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс Epic расширяет Task и хранит список id подзадач.
 * Используется для группировки подзадач в рамках одной большой задачи.
 */
public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

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

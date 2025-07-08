package com.yandex.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс Epic расширяет Task и хранит список id подзадач.
 * Используется для группировки подзадач в рамках одной большой задачи.
 */
public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW);
    }

    public Epic(Epic other) {
        super(other.getTitle(), other.getDescription(), other.getStatus());
        this.setId(other.getId());
        this.subtaskIds = new ArrayList<>(other.getSubtaskIds());
    }

    /**
     * Получает список всех подзадач (возвращает копию).
     */
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    /**
     * Добавляет id в список подзадач эпика.
     */
    public void addSubtaskId(int id) {
        if (id == this.getId()) {
            throw new IllegalArgumentException("Эпик не может содержать сам себя как подзадачу");
        }
        subtaskIds.add(id);
    }

    /**
     * Удаляет id из списка подзадач эпика.
     */
    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    /**
     * Очищает список подзадач в эпике.
     */
    public void clearSubtasks() {
        subtaskIds.clear();
    }

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

    @Override
    public Epic clone() {
        return new Epic(this);
    }
}

package com.yandex.app.model;

/**
 * Класс Subtask расширяет Task и содержит id своего эпика.
 * Используется для создания подзадач, принадлежащих определённому эпику.
 */
public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public Subtask(Subtask other) {
        super(other.getTitle(), other.getDescription(), other.getStatus());
        this.setId(other.getId());
        this.epicId = other.getEpicId();
    }

    /**
     * Возвращает id Epic.
     */
    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", epicId=" + epicId +
                ", status=" + getStatus() + "}";
    }

    @Override
    public Subtask clone() {
        return new Subtask(this);
    }
}

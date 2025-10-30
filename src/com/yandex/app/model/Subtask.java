package com.yandex.app.model;

/**
 * Класс Subtask расширяет Task и содержит id своего эпика.
 * Используется для создания подзадач, принадлежащих определённому эпику.
 */
public class Subtask extends Task {
    private final int epicId;

    /**
     * Конструктор для создания новой подзадачи с указанием эпика.
     *
     * @param title       название подзадачи
     * @param description описание подзадачи
     * @param status      статус подзадачи
     * @param epicId      id эпика, к которому принадлежит эта подзадача
     */
    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    /**
     * Конструктор копирования для создания новой подзадачи на основе существующей.
     *
     * @param other существующая подзадача для копирования
     */
    public Subtask(Subtask other) {
        super(other.getTitle(), other.getDescription(), other.getStatus());
        this.setId(other.getId());
        this.epicId = other.getEpicId();
    }

    /**
     * Возвращает id Epic.
     *
     * @return id эпика, к которому принадлежит эта подзадача
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
    public TypeTask getType() {
        return TypeTask.SUBTASK;
    }

    /**
     * Создает и возвращает копию текущего объекта Subtask.
     *
     * @return новая копия Subtask
     */
    @Override
    public Subtask clone() {
        return new Subtask(this);
    }
}

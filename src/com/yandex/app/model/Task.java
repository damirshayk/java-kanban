package com.yandex.app.model;

/**
 * Базовый класс Task. Представляет задачу с названием, описанием, статусом и id.
 */
public class Task {

    private String title;
    private String description;
    private int id;
    private TaskStatus status;

    /**
     * Конструктор для создания новой задачи.
     *
     * @param title       название задачи
     * @param description описание задачи
     * @param status      статус задачи
     */
    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    /**
     * Конструктор копирования для создания новой задачи на основе существующей.
     *
     * @param other существующая задача для копирования
     */
    public Task(Task other) {
        this.setTitle(other.getTitle());
        this.setDescription(other.getDescription());
        this.setStatus(other.getStatus());
        this.setId(other.getId());
    }

    /** Возвращает тип задачи (для сериализации). */
    public TypeTask getType() {
        return TypeTask.TASK;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {  // Сеттер id (вызывается менеджером)
        this.id = id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Переопределение toString для отладки
     *
     * @return строковое представление задачи
     */
    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

    /**
     * Переопределение equals и hashCode для корректного сравнения задач по id
     * (важно для истории просмотров и других коллекций).
     * Если id совпадают, считаем задачи одинаковыми.
     *
     * @return true если задачи равны по id
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Task task)) return false;
        return getId() == task.getId();
    }

    /**
     * Переопределение hashCode для корректной работы в хэш-коллекциях.
     * Используем только id, так как он уникален для каждой задачи.
     *
     * @return хэш-код задачи
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }

    /**
     * Создает и возвращает копию текущего объекта Task.
     *
     * @return новая копия Task
     */
    @Override
    public Task clone() {
        return new Task(this);
    }
}
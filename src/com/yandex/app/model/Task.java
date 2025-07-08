package com.yandex.app.model;

/**
 * Базовый класс Task. Представляет задачу с названием, описанием, статусом и id.
 */
public class Task {
    /**
     * Ну да, private защищает поля отовсюду вне этого класса. Нужны гетеры/сеттеры
     */
    private String     title;
    private String     description;
    private int        id;
    private TaskStatus status;

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(Task other) {
        this.setTitle(other.getTitle());
        this.setDescription(other.getDescription());
        this.setStatus(other.getStatus());
        this.setId(other.getId());
    }

    /**
     * Получает наименование
     */
    public String getTitle() {
        return title;
    }

    /**
     * Получает описание
     */
    public String getDescription() {
        return description;
    }

    /**
     * Получает id
     */
    public int getId() {
        return id;
    }

    /**
     * Получает статус
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Меняет наименование
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Меняет описание
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Меняет id
     */
    public void setId(int id) {  // Сеттер id (вызывается менеджером)
        this.id = id;
    }

    /**
     * Меняет статус
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Переопределение toString для отладки
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Task task)) return false;
        return getId() == task.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }

    @Override
    public Task clone() {
        return new Task(this);
    }
}
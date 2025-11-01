package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Базовый класс Task. Представляет задачу с названием, описанием, статусом и id.
 */
public class Task {

    private String title;
    private String description;
    private int id;
    private TaskStatus status;

    protected Duration duration;         // длительность задачи (в минутах)
    protected LocalDateTime startTime;   // время начала задачи

    /**
     * Конструктор для создания новой задачи.
     *
     * @param title       название задачи
     * @param description описание задачи
     * @param status      статус задачи
     */
    public Task(String title, String description, TaskStatus status) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.status = Objects.requireNonNullElse(status, TaskStatus.NEW);
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
        this.duration = other.duration;
        this.startTime = other.startTime;
    }

    /**
     * Конструктор для восстановления задачи с id.
     *
     * @param id          идентификатор задачи
     * @param title       название
     * @param description описание
     * @param status      статус
     */
    public Task(int id, String title, String description, TaskStatus status) {
        this(title, description, status); // не знал что так можно вызывать конструктор
        this.id = id;
    }

    /**
     * Конструктор с параметрами времени и продолжительности.
     *
     * @param id          идентификатор задачи
     * @param title       название
     * @param description описание
     * @param status      статус
     * @param duration    длительность задачи
     * @param startTime   время начала задачи
     */
    public Task(int id,
                String title,
                String description,
                TaskStatus status,
                Duration duration,
                LocalDateTime startTime) {
        this(id, title, description, status); // вызов конструктора
        this.duration = duration;
        this.startTime = startTime;
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
        this.title = Objects.requireNonNull(title, "title must not be null");
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "description must not be null");
    }

    public void setId(int id) {  // Сеттер id (вызывается менеджером)
        this.id = id;
    }

    public void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /** Возвращает длительность задачи в виде Optional<Duration>. */
    public Optional<Duration> getDuration() {
        return Optional.ofNullable(duration);
    }

    /** Устанавливает длительность задачи. */
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /** Возвращает время начала задачи в виде Optional<LocalDateTime>. */
    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    /** Устанавливает время начала задачи. */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Возвращает предполагаемое время окончания задачи.
     * Если duration или startTime не заданы — возвращает пустой Optional.
     */
    public Optional<LocalDateTime> getEndTime() {
        return getStartTime().flatMap(start ->
                getDuration().map(start::plus));
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
                ", duration=" + duration +
                ", startTime=" + startTime +
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
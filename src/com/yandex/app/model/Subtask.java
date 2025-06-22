package com.yandex.app.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status); //Вызов конструктора родительского класса Task
        this.epicId = epicId;
    }

    public int getEpicId() { //Получение id эпика
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + getId() +
                ", title='" + getTitle() +
                "', epicId=" + epicId +
                ", status=" + getStatus() + "}";
    }
}
package com.yandex.app.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW); //Вызов конструктора родительского класса Task
    }

    public List<Integer> getSubtaskIds() {             //Получение списка всех подзадач
        return new ArrayList<>(subtaskIds); //Сделал возврат копии списка
    }

    public void addSubtaskId(int id) {                  //Добавление id в список подзадач эпика
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {               //Удаление id из списка подзадач эпика
        subtaskIds.remove(Integer.valueOf(id));         //Важно! Удаление именно по ЗНАЧЕНИЮ, а не по индексу. Долго не понимал ошибку
    }

    public void clearSubtasks() {                       //Очистка списка подзадач в эпике
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
}
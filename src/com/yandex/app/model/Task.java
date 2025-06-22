package com.yandex.app.model;

import java.util.Objects;

public class Task {
    // ну да, private защищает поля отовсюду вне этого класса. Нужны гетеры/сеттеры
    private String     title;
    private String     description;
    private int        id;
    private TaskStatus status;

    public Task(String title, String description, TaskStatus status) {
        this.title = title;             //Название
        this.description = description; //Описание
        this.status = status;           // Статус
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {  // Сеттер id (вызывается менеджером)
        this.id = id;
    }

    public int getId() {                        //Получение id
        return id;
    }

    public TaskStatus getStatus() {             //Получение статуса
        return status;
    }

    public void setStatus(TaskStatus status) {  //Изменение статуса
        this.status = status;
    }

    @Override
    public String toString() {                  //Переопределил toString для отладки
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

    /*Эта автогенерация equals отличается от того что мы проходили в - [Спринт 4/24: 4. (02.06 - 16.06) 🟢 → Тема 3/5:
    Класс Object и его методы → Урок 3/8: Сравниваем объекты с помощью equals(Object)].

    object instanceof Task task одновременно:
        1. Проверяет, что object — это объект класса Task или его наследник.
        *if (this == object) return true;*
        2. Автоматически приводит его к переменной task (если проверка успешна).
        *Task task = (Task) object;*
    Если object не является Task, метод сразу возвращает false. */

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Task task)) return false;
        return id == task.id; //В целом, id у нас уникален. Можно проверять только по нему
    }

    @Override
    public int hashCode() { //Хеш генерируется только по id, так как equals сравнивает только id
        return Objects.hash(id);
    }
}
package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.List;

/**
 * Интерфейс HistoryManager определяет методы для хранения и получения истории просмотров задач.
 */
public interface HistoryManager {

    /**
     * Добавляет задачу в историю просмотров.
     */
    void add(Task task);

    /**
     * Удаляет из истории задачу по id
     */
    void remove(int id);


    /**
     * Возвращает список последних просмотренных задач.
     */
    List<Task> getHistory();
}

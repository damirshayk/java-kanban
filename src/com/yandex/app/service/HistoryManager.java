package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.List;

/**
 * Интерфейс HistoryManager определяет методы для хранения и получения истории просмотров задач.
 */
public interface HistoryManager {

    /**
     * Добавляет задачу в историю просмотров.
     * Если задача уже есть в истории, она перемещается в конец.
     * Если задача равна null, метод просто игнорирует её.
     *
     * @param task задача для добавления в историю
     */
    void add(Task task);

    /**
     * Удаляет из истории задачу по id
     *
     * @param id идентификатор задачи
     */
    void remove(int id);


    /**
     * Возвращает список последних просмотренных задач.
     *
     * @return список задач в порядке просмотра (от старых к новым)
     */
    List<Task> getHistory();
}

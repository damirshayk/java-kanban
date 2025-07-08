package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс InMemoryHistoryManager реализует интерфейс HistoryManager.
 * Хранит последние 10 просмотренных задач в памяти.
 */
public class InMemoryHistoryManager implements HistoryManager {

    /** Максимальный размер истории просмотров. */
    private static final int MAX_HISTORY_SIZE = 10;

    /** Список, хранящий историю просмотренных задач. */
    private final List<Task> history = new ArrayList<>();

    /**
     * Добавляет задачу в историю просмотров.
     * Если достигнут максимальный размер истории, удаляет самый старый просмотр.
     */
    @Override
    public void add(Task task) {
        // Проверяем, достигнут ли лимит истории
        if (history.size() == MAX_HISTORY_SIZE) {
            history.remove(0); // Удаляем первый (наиболее старый) элемент списка
        }
        history.add(task.clone()); // Добавляем задачу в конец списка как последнюю просмотренную
    }

    /**
     * Возвращает копию списка истории просмотров задач.
     */
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Удаление задачи по id из истории
     */
    @Override
    public void remove(int id) {
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getId() == id) {
                history.remove(i);
                break;
            }
        }
    }

}

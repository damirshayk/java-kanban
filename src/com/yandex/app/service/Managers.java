package com.yandex.app.service;

import java.io.File;

/**
 * Утилитарный класс Managers предоставляет методы для получения реализаций интерфейсов TaskManager и HistoryManager.
 * Используется для создания менеджеров без указания их конкретных реализаций.
 */
public class Managers {

    /**
     * Возвращает реализацию TaskManager по умолчанию.
     * В данном случае это FileBackedTaskManager с файлом "tasks.csv".
     *
     * @return экземпляр TaskManager
     */
    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File("tasks.csv"));
    }

    /**
     * Возвращает реализацию HistoryManager по умолчанию.
     * В данном случае это InMemoryHistoryManager.
     *
     * @return экземпляр HistoryManager
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

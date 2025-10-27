package com.yandex.app.service;

import java.io.File;

/**
 * Утилитарный класс Managers предоставляет методы для получения реализаций интерфейсов TaskManager и HistoryManager.
 * Используется для создания менеджеров без указания их конкретных реализаций.
 */
public class Managers {

    /**
     * Возвращает реализацию TaskManager по умолчанию.
     * В данном случае это InMemoryTaskManager.
     *
     * @return экземпляр TaskManager
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
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

    /**
     * Возвращает реализацию FileBackedTaskManager с указанным файлом для сохранения данных.
     *
     * @param file файл для сохранения данных
     * @return экземпляр FileBackedTaskManager
     */
    public static FileBackedTaskManager getFileBackedManager(File file) {
        return new FileBackedTaskManager(file);
    }
}

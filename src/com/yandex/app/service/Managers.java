package com.yandex.app.service;

/**
 * Утилитарный класс Managers предоставляет методы для получения реализаций интерфейсов TaskManager и HistoryManager.
 * Используется для создания менеджеров без указания их конкретных реализаций.
 */
public class Managers {

    /**
     * Возвращает реализацию TaskManager по умолчанию.
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    /**
     * Возвращает реализацию HistoryManager по умолчанию.
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

package com.yandex.app.service;

/**
 * Исключение, возникающее при ошибках сохранения данных менеджера задач.
 */
public class ManagerSaveException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     * @param message описание ошибки.
     */
    public ManagerSaveException(String message) {
        super(message);
    }

    /**
     * Конструктор исключения с сообщением и причиной.
     * @param message описание ошибки.
     * @param cause причина ошибки.
     */
    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}

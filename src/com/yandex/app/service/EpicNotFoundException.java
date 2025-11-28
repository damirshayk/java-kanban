package com.yandex.app.service;

/**
 * Исключение, возникающее при попытке доступа к несуществующему эпику.
 */
public class EpicNotFoundException extends RuntimeException {

    public EpicNotFoundException(String message) {
        super(message);
    }
}
package com.yandex.app.http;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Тестовый набор, объединяющий все HTTP-тесты сервера задач.
 * Позволяет запускать их одним кликом, аналогично TaskManagerTest.
 */
@Suite
@SelectClasses({
        HttpTaskManagerTasksTest.class,
        HttpTaskManagerSubtasksTest.class,
        HttpTaskManagerEpicsTest.class,
        HttpTaskManagerHistoryTest.class,
        HttpTaskManagerPrioritizedTest.class
})
public class HttpTaskServerTestSuite {
    // Аннотации делают всю работу.
}

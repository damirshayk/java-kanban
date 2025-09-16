package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.*;

/**
 * Класс InMemoryHistoryManager реализует интерфейс HistoryManager.
 * Хранит историю просмотренных задач с использованием двусвязного списка и HashMap.
 * Обеспечивает удаление и добавление за O(1).
 */
public class InMemoryHistoryManager implements HistoryManager {

    // Узел двусвязного списка
    private static class Node {
        Task task;
        Node prev;
        Node next;

        // Конструктор узла
        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    // Голова и хвост списка
    private Node head;
    private Node tail;

    //Словарь: id задачи → узел списка
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    /**
     * Добавляет задачу в историю просмотров.
     * Если задача уже есть в истории, она перемещается в конец.
     * Если задача равна null, метод просто игнорирует её.
     *
     * @param task
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return; // просто игнорируем null
        }

        // Если задача уже есть — удалим старый узел
        if (nodeMap.containsKey(task.getId())) {
            remove(task.getId());
        }

        // Создаём новый узел и добавляем его в конец списка
        Node newNode = linkLast(task.clone());

        // Обновляем словарь
        nodeMap.put(task.getId(), newNode);
    }

    /**
     * Удаляет из истории задачу по id
     *
     * @param id
     */
    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    /**
     * Возвращает список последних просмотренных задач.
     *
     * @return список задач
     */
    @Override
    public List<Task> getHistory() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    /**
     * Добавляет узел в конец двусвязного списка
     *
     * @return новый узел
     */
    private Node linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        return newNode;
    }

    /**
     * Удаляет узел из списка
     *
     * @param node узел для удаления
     */
    private void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }
    }
}

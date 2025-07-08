package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.TaskManager;
import com.yandex.app.service.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        //Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task task1 = new Task("Переезд", "Упаковать вещи и переехать", TaskStatus.NEW);
        manager.addTask(task1);

        Task task2 = new Task("Позавтракать", "Приготовить луковый угар", TaskStatus.NEW); //Рецепт есть на ютубе
        manager.addTask(task2);

        Epic epic1 = new Epic("Организовать праздник", "День рождения мамы");
        manager.addEpic(epic1);

        Epic epic2 = new Epic("Переоформить документы", "Квартира");
        manager.addEpic(epic2);

        Subtask sub1 = new Subtask("Купить еду", "Купить продуктов",
                TaskStatus.NEW, epic1.getId());
        manager.addSubtask(sub1);

        Subtask sub2 = new Subtask("Купить фигурку Сайтамы", "Найти магазин аниме атрибутики",
                TaskStatus.NEW, epic1.getId());
        manager.addSubtask(sub2);

        Subtask sub3 = new Subtask("Справка", "Запросить справку на госуслугах",
                TaskStatus.NEW, epic2.getId());
        manager.addSubtask(sub3);

        // Вызовы методов получения задач, чтобы наполнить историю просмотров
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(sub1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epic2.getId());
        manager.getSubtaskById(sub2.getId());
        manager.getSubtaskById(sub3.getId());

        // Вывод всех задач и истории просмотров после их создания
        printAllTasks(manager);

        //Измените статусы созданных объектов, распечатайте их.
        //Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        task1.setStatus(TaskStatus.IN_PROGRESS);
        sub1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        manager.updateSubtask(sub1);

        System.out.println("\nИстория после обновлений:");
        printAllTasks(manager);

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("\nИстория после удаления:");
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nЗадачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Subtask subtask : manager.getSubtasksOfEpic(epic.getId())) {
                System.out.println("--> " + subtask);
            }
        }

        System.out.println("\nПодзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("\nИстория просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
} // Только Бог-Император простит.

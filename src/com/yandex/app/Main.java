package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task task1 = new Task("Переезд", "Упаковать вещи и переехать",
                TaskStatus.NEW);
        manager.addTask(task1);

        Task task2 = new Task("Позавтракать", "Приготовить луковый угар",
                TaskStatus.NEW); //Рецепт есть на ютубе
        manager.addTask(task2);

        Epic epic1 = new Epic("Организовать праздник", "День рождения мамы");
        manager.addEpic(epic1);

        Epic epic2 = new Epic("Переоформить документы", "Квартира");
        manager.addEpic(epic2);

        Subtask sub1 = new Subtask("Купить еду", "Купить продуктов",
                TaskStatus.NEW, epic1.getId());
        manager.addSubtask(sub1);

        Subtask sub2 = new Subtask("Найти место в жизни", "Поиск времени на поиск места",
                TaskStatus.NEW, epic1.getId());
        manager.addSubtask(sub2);

        Subtask sub3 = new Subtask("Справка о несудимости", "Запросить справку на госуслугах",
                TaskStatus.NEW, epic2.getId());
        manager.addSubtask(sub3);

        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());

        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());

        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());

        //Измените статусы созданных объектов, распечатайте их.
        //Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        task1.setStatus(TaskStatus.IN_PROGRESS);
        sub1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        manager.updateSubtask(sub1);

        System.out.println("\nПосле обновлений:");
        System.out.println(manager.getEpicById(epic1.getId()));

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("\nПосле удаления:");
        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
    }
}
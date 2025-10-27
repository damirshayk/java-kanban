package com.yandex.app.service;

import com.yandex.app.model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Менеджер задач, сохраняющий данные в CSV-файл.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file; // Файл для сохранения данных

    /**
     * Конструктор менеджера с указанием файла для сохранения.
     *
     * @param file файл для сохранения данных.
     */
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    /**
     * Сохраняет все задачи в файл в формате CSV.
     */
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + file.getName(), e);
        }
    }

    /**
     * Преобразует задачу в CSV-строку.
     */
    private String toString(Task task) {
        String type;
        if (task instanceof Epic) {
            type = "EPIC";
        } else if (task instanceof Subtask) {
            type = "SUBTASK";
        } else {
            type = "TASK";
        }

        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                type,
                clearStringForCSV(task.getTitle()),
                task.getStatus().name(),
                clearStringForCSV(task.getDescription()),
                epicId);
    }

    /**
     * Очищает строку от символов, мешающих формату CSV.
     */
    private String clearStringForCSV(String value) {
        if (value == null) return "";
        return value
                .replace(",", " ")
                .replace("\n", " ")
                .replace("\"", "")
                .replace("'", "")
                .replace(";", "")
                .trim();
    }

    /**
     * Преобразует CSV-строку в задачу.
     */
    private static Task fromString(String line) {
        String[] fields = line.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        String type = fields[1];
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        return switch (type) {
            case "TASK" -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                yield task;
            }
            case "EPIC" -> {
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                yield epic;
            }
            case "SUBTASK" -> {
                int epicId = Integer.parseInt(fields[5]);
                Subtask sub = new Subtask(name, description, status, epicId);
                sub.setId(id);
                yield sub;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        };
    }

    /**
     * Загружает данные менеджера из файла.
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                return manager;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) { // пропускаем заголовок
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                Task task = fromString(line);
                if (task instanceof Epic epic) {
                    manager.addEpic(epic);
                } else if (task instanceof Subtask subtask) {
                    manager.addSubtask(subtask);
                } else {
                    manager.addTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + file.getName(), e);
        }
        return manager;
    }

    // ==== Переопределяем методы + save() ====

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    /**
     * Пример использования: создаёт менеджер, сохраняет и затем загружает данные.
     */
    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task t1 = new Task("Переезд", "Собрать вещи", TaskStatus.NEW);
        manager.addTask(t1);

        Epic e1 = new Epic("Организация праздника", "День рождения мамы");
        manager.addEpic(e1);

        Subtask s1 = new Subtask("Купить еду", "Торт и напитки", TaskStatus.NEW, e1.getId());
        manager.addSubtask(s1);

        System.out.println("\nСохранено в файл. Перезапускаем менеджер...\n");

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Загруженные задачи:");
        loaded.getAllTasks().forEach(System.out::println);
        loaded.getAllEpics().forEach(System.out::println);
        loaded.getAllSubtasks().forEach(System.out::println);
    }
}

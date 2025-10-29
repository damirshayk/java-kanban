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
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

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
            writer.write(CSV_HEADER + "\n");

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
        String epicId = "";
        if (task.getType() == TypeTask.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                clearStringForCSV(task.getTitle()),
                task.getStatus().name(),
                clearStringForCSV(task.getDescription()),
                epicId);
    }

    /**
     * Экранирует строку для корректного сохранения в CSV.
     *
     * @param value исходная строка.
     */
    private String clearStringForCSV(String value) {
        if (value == null) return "\"\""; // пустое поле
        String escaped = value
                .replace("\"", "\"\"")   // удваиваем кавычки
                .replace("\r", "")       // удаляем возвраты каретки
                .replace("\n", "")       // удаляем переводы строк
                .replace("\t", "")       // удаляем табуляции
                .trim();
        return "\"" + escaped + "\"";
    }

    /**
     * Декодирует строку из CSV-формата.
     *
     * @param value строка в CSV-формате.
     */
    private static String unescapeCSV(String value) {
        if (value == null || value.isEmpty()) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\"\"", "\"");
    }

    /**
     * Безопасно парсит строку CSV с учётом кавычек и запятых.
     */
    private static List<String> parseCSVLine(String line) {
        // Создаём список, куда будем добавлять найденные поля
        List<String> result = new ArrayList<>();
        // Если строка пустая, возвращаем пустой список
        if (line == null || line.isEmpty()) return result;
        // Буфер для текущего поля
        StringBuilder current = new StringBuilder();
        // Флаг, показывающий, находимся ли мы сейчас "внутри кавычек"
        boolean inQuotes = false;
        // Проходим по каждому символу строки
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i); // текущий символ
            // Если встретили кавычку
            if (c == '"') {
                // Если уже находимся внутри кавычек, и следующая тоже кавычка — это экранированная кавычка ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); // добавляем одну кавычку в значение поля
                    i++;                 // пропускаем следующую кавычку
                } else {
                    // Меняем состояние — входим или выходим из кавычек
                    inQuotes = !inQuotes;
                }
                // Если встретили запятую, и мы НЕ внутри кавычек
            } else if (c == ',' && !inQuotes) {
                // Завершаем текущее поле, добавляем его в список
                result.add(current.toString());
                // Очищаем буфер, чтобы начать собирать следующее поле
                current.setLength(0);
                // Любой другой символ добавляем в текущее поле
            } else {
                current.append(c);
            }
        }
        // Добавляем последнее поле после выхода из цикла (после последней запятой)
        result.add(current.toString());
        // Возвращаем итоговый список полей
        return result;
    }

    /**
     * Преобразует CSV-строку в задачу.
     */
    private static Task fromString(String line) {
        List<String> fields = parseCSVLine(line);
        int id = Integer.parseInt(fields.get(0));
        TypeTask type = TypeTask.valueOf(fields.get(1));
        String name = unescapeCSV(fields.get(2));
        TaskStatus status = TaskStatus.valueOf(fields.get(3));
        String description = unescapeCSV(fields.get(4));

        switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields.get(5));
                Subtask sub = new Subtask(name, description, status, epicId);
                sub.setId(id);
                return sub;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    /**
     * Загружает менеджер задач из указанного файла.
     *
     * @param file файл для загрузки данных.
     * @return загруженный менеджер задач.
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            // Если файла нет или он пуст — просто вернуть пустой менеджер
            if (!file.exists() || file.length() == 0) {
                return manager;
            }

            System.out.println("Загружаю данные из файла: " + file.getAbsolutePath()); //для отладки

            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.isEmpty()) {
                // только заголовок или пустой файл
                return manager;
            }

            String header = lines.getFirst().trim();
            if (!header.equals(CSV_HEADER)) {
                throw new ManagerSaveException("Файл имеет неверный формат CSV: ожидается заголовок \""
                        + CSV_HEADER + "\", найдено: " + header);
            }

            // Пропускаем первую строку с заголовками
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                try {
                    Task task = fromString(line);

                    if (task instanceof Epic epic) {
                        manager.addEpic(epic);
                    } else if (task instanceof Subtask subtask) {
                        manager.addSubtask(subtask);
                    } else {
                        manager.addTask(task);
                    }

                } catch (Exception parseError) {
                    // Если строка повреждена — выбрасываем своё исключение, как ожидает тест
                    throw new ManagerSaveException("Ошибка парсинга CSV-строки: " + line, parseError);
                }
            }

        } catch (IOException e) {
            // Ошибка чтения самого файла
            throw new ManagerSaveException("Ошибка при чтении файла: " + file.getName(), e);
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

        Task t1 = new Task("Проверка кавычек", "Кавычки \"внутри\" строки", TaskStatus.NEW);
        manager.addTask(t1);

        Epic e1 = new Epic("Проверка запятой", "Задача, с запятой  в описании");
        manager.addEpic(e1);

        Subtask s1 = new Subtask("\"Проверка\", с запятой после кавычки", "", TaskStatus.NEW, e1.getId());
        manager.addSubtask(s1);

        System.out.println("\nСохранено в файл. Перезапускаем менеджер...\n");

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Загруженные задачи:");
        //Про forEach только узнал из интернета, очень удобно!
        loaded.getAllTasks().forEach(System.out::println);
        loaded.getAllEpics().forEach(System.out::println);
        loaded.getAllSubtasks().forEach(System.out::println);
    }
}
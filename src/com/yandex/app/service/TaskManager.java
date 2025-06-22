package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.TaskStatus;

import java.util.*;

public class TaskManager {
    private int                         nextId   = 1; //Хватит полей id, будет nextId
    //Чтобы получать разные типы задач, вы можете создать три структуры HashMap: по одной на каждый из видов задач.
    private final Map<Integer, Task>    tasks    = new HashMap<>();
    private final Map<Integer, Epic>    epics    = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    /*Для генерации идентификаторов можно использовать числовое поле-счётчик внутри класса TaskManager,
    увеличивая его на 1, когда нужно получить новое значение.*/
    public int generateId() { //Подсказка: как создавать идентификаторы.
        return nextId++;
    }

    public void addTask(Task task) {
        //Добавление в мапу task новой пары ключ-значение (уникальный id и задача)
        int id = generateId(); //добавил автогенерацию id при добавлении задачи
        task.setId(id);
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        //Добавление в мапу epic новой пары ключ-значение (уникальный id и эпик)
        int id = generateId(); //добавил автогенерацию id при добавлении эпика
        epic.setId(id);
        epics.put(epic.getId(), epic);
    }

    public void addSubtask(Subtask subtask) {
        //Добавление в мапу subtask новой пары ключ-значение (уникальный id и подзадача)
        int id = generateId(); //добавил автогенерацию id при добавлении подзадачи
        subtask.setId(id);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId()); //Получаем id из подзадачи
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);                         //Вызов метода обновления эпик статусов
        }
    }

    public List<Task> getAllTasks() {                       //Получение списка всех задач
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {                       //Получение списка всех эпиков
        return List.copyOf(epics.values()); //после изменения getSubtaskIds
    }

    public List<Subtask> getAllSubtasks() {                 //Получение списка всех подзадач
        return new ArrayList<>(subtasks.values());
    }

    public Task getTaskById(int id) {                       //Получение задачи по id
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {                       //Получение пика по id
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {                 //Получение подзадачи по id
        return subtasks.get(id);
    }

    public void updateTask(Task task) {                     //Обновление задачи
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {                     //Обновление эпика и его статуса
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);             //Обновление подзадачи
        Epic epic = epics.get(subtask.getEpicId());         //Получаем эпик с этим id
        if (epic != null) updateEpicStatus(epic);           //Если эпик найден, то обновляем статус
    }

    public void deleteTaskById(int id) {                    //Удаление задачи по id
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {                    //Удаление эпика по id
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    public void deleteSubtaskById(int id) {                 //Удаление подзадачи по id
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    public void deleteAllTasks() {                         //Удаление всех эпиков
        tasks.clear();
    }

    public void deleteAllEpics() {                         //Удаление всех эпиков
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {                      //Удаление всех подзадач
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public List<Subtask> getSubtasksOfEpic(int epicId) {   //Получение списка подзадач из эпика по id
        Epic epic = epics.get(epicId);
        List<Subtask> result = new ArrayList<>();
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                result.add(subtasks.get(subtaskId));
            }
        }
        return result;
    }

    private void updateEpicStatus(Epic epic) {              //Автоматическое обновление статуса эпика
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        //Комментарий для себя. Это надо запомнить, тут я долго тупил.
        boolean allNew = true;                              //Допустим, что все подзадачи новые
        boolean allDone = true;                             //Допустим, что все подзадачи завершились

        for (int id : subtaskIds) {                         //Перебор всех подзадач
            //Услышал слово "зачейнить" от Воронова Филипа, вроде бы на IV Q&A-вебинаре. Познал мир и вот что вышло. Меньше переменных.
            TaskStatus status = subtasks.get(id).getStatus();   //Получение статуса по id
            if (status != TaskStatus.NEW) allNew = false;       //Если попался статус NEW, то всё - allNew = false
            if (status != TaskStatus.DONE) allDone = false;     //тут аналогично
        }

        /*
        Если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть - NEW.
        Если все подзадачи имеют статус DONE, то и эпик считается завершённым — DONE.
        Во всех остальных случаях статус должен быть - IN_PROGRESS.
         */
        if (allDone) epic.setStatus(TaskStatus.DONE);
        else if (allNew) epic.setStatus(TaskStatus.NEW);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }
}
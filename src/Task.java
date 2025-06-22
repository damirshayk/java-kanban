import java.util.Objects;

public class Task { // для полей модификатор protected
    protected String     title;
    protected String     description;
    protected int        id;
    protected TaskStatus status;

    public Task(String title, String description, int id, TaskStatus status) {
        this.title = title;             //Название
        this.description = description; //Описание
        this.id = id;                   //Уникальный идентификационный номер задачи
        this.status = status;           // Статус
    }

    public int getId() {                        //Получение id
        return id;
    }

    public TaskStatus getStatus() {             //Получение статуса
        return status;
    }

    public void setStatus(TaskStatus status) {  //Изменение статуса
        this.status = status;
    }

    @Override
    public String toString() {                  //Переопределил toString для отладки
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

    /*Эта автогенерация equals отличается от того что мы проходили в - [Спринт 4/24: 4. (02.06 - 16.06) 🟢 → Тема 3/5:
    Класс Object и его методы → Урок 3/8: Сравниваем объекты с помощью equals(Object)].

    object instanceof Task task одновременно:
        1. Проверяет, что object — это объект класса Task или его наследник.
        *if (this == object) return true;*
        2. Автоматически приводит его к переменной task (если проверка успешна).
        *Task task = (Task) object;*
    Если object не является Task, метод сразу возвращает false. */

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Task task)) return false;
        return id == task.id &&  //В целом, id у нас уникален. Можно проверять только по нему
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                status == task.status;
    }

    @Override
    public int hashCode() { //Тут просто генерация хеша из 4-х параметров
        return Objects.hash(title, description, id, status);
    }
}
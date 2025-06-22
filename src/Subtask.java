public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int id, TaskStatus status, int epicId) {
        super(title, description, id, status); //Вызов конструктора родительского класса Task
        this.epicId = epicId;
    }

    public int getEpicId() { //Получение id эпика
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + id +
                ", title='" + title +
                "', epicId=" + epicId +
                ", status=" + status + "}";
    }
}
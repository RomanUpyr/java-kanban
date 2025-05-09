package model;

// Создаем класс для подзадач, наследуем от model.Task
public class Subtask extends Task {
    private final int epicId; // Индентификатор для эпика, к которому принадлежит задача

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        if (epicId <= 0) throw new IllegalArgumentException("ID эпика должно быть положительным");
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        if (epicId <= 0) throw new IllegalArgumentException("ID эпика должно быть положительным");
        this.epicId = epicId;
    }


    // Создаем геттер
    public int getEpicId() {
        return epicId;
    }

    // Переопределяем метод toString для удобного вывода информации о подзадаче
    @Override
    public String toString() {
        return "model.Task{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}

// Создаем класс для подзадач, наследуем от Task
public class Subtask extends Task{
    private int epicId; // Индентификатор для эпика, к которому принадлежит задача

    // Конструктор для создания подзадачи
    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status); // Вызываем конструктор родительского класса
        this.epicId = epicId;
    }

    // Создаем геттер
    public int getEpicId() {
        return epicId;
    }

    // Переопределяем метод toString для удобного вывода информации о подзадаче
    @Override
    public String toString() {
        return "Task{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                "epicId=" + epicId +
                '}';
    }
}

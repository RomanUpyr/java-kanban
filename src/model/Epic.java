package model;

import java.util.ArrayList;
import java.util.List;

// Класса для эпиков, наследуем от model.Task
public class Epic extends Task {
    private List<Integer> subtaskIds; // Список ids для подзадач входящих в эпик

    // Конструктор для создания эпика
    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW); // Эпик всегда создается со статусом NEW
        this.subtaskIds = new ArrayList<>();// Инициализируем список подзадач
    }

    // Геттер для списка идентификаторов подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Метод для добавления индентификатора подзадачи в эпик
    public void addSubtaskId(int subtaskID) {
        subtaskIds.add(subtaskID);
    }

    // Метод для удаления индентификатора подзадачи из эпика
    public void removeSubtaskId(int subtaskID) {
        subtaskIds.remove((Integer) subtaskID); // Удаляем по значению
    }

    // Переопределяем метод toString для удобного вывода информации об эпике
    @Override
    public String toString() {
        return "model.Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}

package model;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import manager.TaskManager;

// Класс для эпиков, наследуем от model.Task
public class Epic extends Task {
    private final List<Integer> subtaskIds; // Список ids для подзадач входящих в эпик
    private LocalDateTime endTime;

    // Конструктор для создания эпика
    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW); // Эпик всегда создается со статусом NEW
        this.subtaskIds = new ArrayList<>();// Инициализируем список подзадач
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    // Геттер для списка идентификаторов подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Метод для добавления индентификатора подзадачи в эпик
    public void addSubtaskId(int subtaskID) {
        if (subtaskID <= 0) {
            throw new IllegalArgumentException("ID подзадачи должен быть положительным числом");
        }
        subtaskIds.add(subtaskID);
    }

    // Метод для удаления индентификатора подзадачи из эпика
    public void removeSubtaskId(int subtaskID) {
        subtaskIds.remove((Integer) subtaskID); // Удаляем по значению
    }

    /**
     * Обновляет временные параметры эпика на основе подзадач
     *
     * @param taskManager менеджер задач для доступа к подзадачам
     */
    public void updateEpicFields(TaskManager taskManager) {
        if (subtaskIds.isEmpty()) {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
        }

        List<Subtask> subtasks = taskManager.getSubtasksByEpicId(this.getId());

        // Обновление времени начала (самая ранняя подзадача)
        this.startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Обновление продолжительности (сумма подзадач)
        this.duration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        // Обновление времени окончания (самая поздняя подзадача)
        this.endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }


    // Переопределяем метод toString для удобного вывода информации об эпике
    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }
}

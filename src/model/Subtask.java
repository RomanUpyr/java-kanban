package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

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

    public Subtask(int id, String name, String description, Status status,
                   int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    // Создаем геттер
    public int getEpicId() {
        return epicId;
    }

    // Переопределяем метод toString для удобного вывода информации о подзадаче
    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }


    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}

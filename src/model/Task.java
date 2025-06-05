package model;

import com.google.gson.annotations.Expose;

import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;

// Создаем базовый класс для задач
public class Task {
    private static int counter = 0;
    @Expose
    protected int id; // Уникальный идентификатор задачи
    @Expose
    protected String name; // Название задачи
    @Expose
    protected String description; // Описание задачи
    @Expose
    protected Status status; // Текущий статус задачи
    protected Duration duration; // Продолжительность задачи в минутах
    protected LocalDateTime startTime; // Дата и время начала выполнения задачи

    // Конструкторы для новых задач
    public Task(String name, String description, Status status) {
        this.id = ++counter;
        this.name = Objects.requireNonNull(name, "Имя задачи не может быть null");
        this.description = Objects.requireNonNull(description, "Описание задачи не может быть null");
        this.status = Objects.requireNonNull(status, "Статус задачи не может быть null");
    }

    public Task(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.id = ++counter;
        this.name = Objects.requireNonNull(name, "Имя задачи не может быть null");
        this.description = Objects.requireNonNull(description, "Описание задачи не может быть null");
        this.status = Objects.requireNonNull(status, "Статус задачи не может быть null");
        this.duration = duration;
        this.startTime = startTime;
    }

    // Конструкторы для существующих задач (при загрузке из хранилища)
    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Имя задачи не может быть null");
        this.description = Objects.requireNonNull(description, "Описание задачи не может быть null");
        this.status = Objects.requireNonNull(status, "Статус задачи не может быть null");
        if (id > counter) counter = id;
    }

    public Task(int id, String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Имя задачи не может быть null");
        this.description = Objects.requireNonNull(description, "Описание задачи не может быть null");
        this.status = Objects.requireNonNull(status, "Статус задачи не может быть null");
        if (id > counter) counter = id;
        this.duration = duration;
        this.startTime = startTime;
    }

    // Создаем геттеры для полей задачи
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * Рассчитываем время окончания задачи
     *
     * @return LocalDateTime время окончания или null, если не задано время начала или продолжительность
     */
    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    // Переопределяем метод toString для удобного вывода информации о задаче
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }

    // Переопределяем equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    // Переопределяем hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static void resetCounter() {
        counter = 0;
    }
}

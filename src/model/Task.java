package model;

import java.util.Objects;

// Создаем базовый класс для задач
public class Task {
    private int id; // Уникальный идентификатор задачи
    private String name; // Название задачи
    private String description; // Описание задачи
    private Status status; // Текущий статус задачи

    // Создаем конструктор для создания задачи
    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
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

    // Создаем сеттеры для полей задачи
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Переопределяем метод toString для удобного вывода информации о задаче
    @Override
    public String toString() {
        return "model.Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
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
}

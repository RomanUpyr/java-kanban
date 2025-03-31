package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() { // Проверяем, что две задачи с одинаковыми id считаются равными
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Different description", Status.IN_PROGRESS);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void taskShouldNotBeEqualToNull() { // Проверяем, что задача не равна null
        Task task = new Task(1, "Task", "Description", Status.NEW);
        assertNotEquals(null, task, "Задача не должна быть равна null");
    }

    @Test
    void taskShouldNotBeEqualToObjectOfDifferentClass() { // Проверяем, что задача не равна объекту другого класса
        Task task = new Task(1, "Task", "Description", Status.NEW);
        Object differentObject = new Object(); // Создаем объект другого класса
        assertNotEquals(differentObject, task, "Задача не должна быть равна объекту другого класса");
    }

    @Test
    void tasksWithSameIdShouldHaveSameHashCode() { // Проверяем, что у задач с одинаковым id одинаковый хэш-код
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Different description", Status.IN_PROGRESS);

        assertEquals(task1.hashCode(), task2.hashCode(), "Хэш-коды задач с одинаковым id должны совпадать");
    }
}
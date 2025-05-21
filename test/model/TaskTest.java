package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

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
    @Test
    void testTaskTimeManagement() {
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(2);
        Task task = new Task("Task", "Description",Status.NEW, duration, startTime);

        assertEquals(startTime, task.getStartTime());
        assertEquals(duration, task.getDuration());
        assertEquals(startTime.plus(duration), task.getEndTime());
    }

    @Test
    void testTaskWithoutTime() {
        Task task = new Task("Task", "Description", Status.NEW);

        assertNull(task.getStartTime());
        assertNull(task.getDuration());
        assertNull(task.getEndTime());
    }

    @Test
    void testTaskToString() {
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(1);
        Task task = new Task("Task", "Description", Status.NEW, duration, startTime);

        String expected = "Task{id=0, title='Task', description='Description', " +
                "status=NEW, startTime=2023-01-01T10:00, duration=PT1H}";
        assertEquals(expected, task.toString());
    }

}
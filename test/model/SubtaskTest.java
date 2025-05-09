package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void subtasksWithSameIdShouldBeEqual() { // Проверяем равенство подзадач по id
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description", Status.NEW, 2);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Different description", Status.DONE, 3);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void subtaskShouldInheritFromTask() { // Проверяем наследование от Task
        Subtask subtask = new Subtask(1, "Subtask", "Description", Status.NEW, 2);
        assertInstanceOf(Task.class, subtask, "Subtask должен наследоваться от Task");
    }

    @Test
    void shouldReturnCorrectEpicId() { // Проверяем корректность возвращаемого epicId
        int epicId = 5;
        Subtask subtask = new Subtask(1, "Subtask", "Description", Status.NEW, epicId);
        assertEquals(epicId, subtask.getEpicId(), "Неверно возвращается epicId");
    }
}
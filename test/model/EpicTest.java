package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicsWithSameIdShouldBeEqual() { // Проверяем равенство эпиков по id
        Epic epic1 = new Epic(1, "Epic 1", "Description");
        Epic epic2 = new Epic(1, "Epic 2", "Different description");

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void epicShouldInheritFromTask() { // Проверяем, что Epic наследуется от Task
        Epic epic = new Epic(1, "Epic", "Description");
        assertInstanceOf(Task.class, epic, "Epic должен наследоваться от Task");
    }

    @Test
    void newEpicShouldHaveEmptySubtasksList() { // Проверяем, чот новый эпик создается с пустым списком задач
        Epic epic = new Epic(1, "Epic", "Description");
        assertTrue(epic.getSubtaskIds().isEmpty(), "Новый эпик должен иметь пустой список подзадач");
    }

    @Test
    void shouldAddAndRemoveSubtaskIds() { // Проверяем добавление и удаление id подзадач из эпика
        Epic epic = new Epic(1, "Epic", "Description");
        epic.addSubtaskId(2);
        assertEquals(1, epic.getSubtaskIds().size(), "Не удалось добавить подзадачу");

        epic.removeSubtaskId(2);
        assertTrue(epic.getSubtaskIds().isEmpty(), "Не удалось удалить подзадачу");
    }
}
package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void setUp() {
        epic = new Epic("Тестовый эпик", "Описание эпика");
        subtask1 = new Subtask(1, "Подзадача 1", "Описание 1", Status.NEW, epic.getId());
        subtask2 = new Subtask(2, "Подзадача 2", "Описание 2", Status.NEW, epic.getId());

        epic.addSubtaskId(subtask1.getId());
        epic.addSubtaskId(subtask2.getId());
    }

    @Test
    @DisplayName("Эпики с одинаковым ID должны быть равны")
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic(1, "Эпик 1", "Описание");
        Epic epic2 = new Epic(1, "Эпик 2", "Другое описание");

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
    }

    @Test
    @DisplayName("Эпик должен наследоваться от Task")
    void epicShouldInheritFromTask() {
        assertInstanceOf(Task.class, epic, "Epic должен наследоваться от Task");
    }

    @Test
    @DisplayName("Новый эпик должен иметь пустой список подзадач")
    void newEpicShouldHaveEmptySubtasksList() {
        Epic newEpic = new Epic("Новый эпик", "Описание");
        assertTrue(newEpic.getSubtaskIds().isEmpty(), "Новый эпик должен иметь пустой список подзадач");
    }

    @Test
    @DisplayName("Должен добавлять и удалять ID подзадач")
    void shouldAddAndRemoveSubtaskIds() {
        Epic testEpic = new Epic(1, "Тестовый эпик", "Описание");

        // Проверяем добавление валидного ID
        testEpic.addSubtaskId(2);
        assertEquals(1, testEpic.getSubtaskIds().size(), "Не удалось добавить подзадачу");

        // Проверяем удаление
        testEpic.removeSubtaskId(2);
        assertTrue(testEpic.getSubtaskIds().isEmpty(), "Не удалось удалить подзадачу");

        // Проверяем добавление невалидного ID
        assertThrows(IllegalArgumentException.class, () -> testEpic.addSubtaskId(-1));
        assertThrows(IllegalArgumentException.class, () -> testEpic.addSubtaskId(0));
    }

    @Test
    @DisplayName("Должен создавать эпик с корректным начальным состоянием")
    void shouldCreateEpicWithCorrectInitialState() {
        assertNotNull(epic.getId(), "ID должен быть сгенерирован");
        assertEquals("Тестовый эпик", epic.getName(), "Название не соответствует");
        assertEquals("Описание эпика", epic.getDescription(), "Описание не соответствует");
        assertEquals(Status.NEW, epic.getStatus(), "Статус по умолчанию должен быть NEW");
        assertEquals(2, epic.getSubtaskIds().size(), "Должно быть 2 подзадачи");
    }

    @Test
    @DisplayName("Должен корректно обрабатывать невалидный ID подзадачи")
    void shouldThrowWhenAddingInvalidSubtaskId() {
        assertThrows(IllegalArgumentException.class, () -> epic.addSubtaskId(-1),
                "Должно выбрасываться исключение при невалидном ID");
    }

    @Test
    @DisplayName("Должен корректно отображаться в строковом представлении")
    void shouldReturnCorrectStringRepresentation() {
        String expected = "Epic{id=" + epic.getId() +
                ", name='Тестовый эпик', description='Описание эпика', status=NEW" +
                ", subtaskIds=" + epic.getSubtaskIds() +
                ", duration=" + epic.getDuration() +
                ", startTime=" + epic.getStartTime() + "}";
        assertEquals(expected, epic.toString(), "Строковое представление не совпадает");
    }
}
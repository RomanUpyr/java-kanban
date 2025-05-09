package manager;

import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager = Managers.getDefault();

    @Test
    void shouldAddAndFindTaskById() {
        Task task = new Task("Task", "Description", Status.NEW);
        taskManager.createTask(task);
        final int taskId = task.getId();

        Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldAddAndFindEpicById() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);
        final int epicId = epic.getId();

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
    }

    @Test
    void shouldAddAndFindSubtaskById() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);
        final int subtaskId = subtask.getId();

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");
    }

    @Test
    void shouldNotConflictTasksWithGeneratedAndProvidedIds() {
        // Создаем задачу с автоматической генерацией ID
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        taskManager.createTask(task1);

        // Создаем вторую задачу с автоматической генерацией ID
        Task task2 = new Task("Task 2", "Description", Status.NEW);
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать");
        assertTrue(task1.getId() > 0, "Должен быть установлен валидный ID");
        assertTrue(task2.getId() > 0, "Должен быть установлен валидный ID");
    }

    @Test
    void shouldNotAddSubtaskToItselfAsEpic() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId());
        subtask.setId(epic.getId()); // Имитируем ситуацию, когда ID подзадачи равен ID эпика

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubtask(subtask);
        }, "Нельзя добавить подзадачу в саму себя как эпик");
    }

    @Test
    void shouldUpdateEpicStatusBasedOnSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        assertEquals(Status.NEW, epic.getStatus(), "Новый эпик должен иметь статус NEW");

        // Добавляем подзадачу со статусом NEW
        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        assertEquals(Status.NEW, epic.getStatus(), "Эпик с NEW подзадачами должен иметь статус NEW");

        // Добавляем подзадачу со статусом DONE
        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.DONE, epic.getId());
        taskManager.createSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Эпик с NEW и DONE подзадачами должен иметь статус IN_PROGRESS");

        // Обновляем все подзадачи на DONE
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.DONE, epic.getStatus(),
                "Эпик со всеми DONE подзадачами должен иметь статус DONE");
    }

    @Test
    void shouldPreserveTaskFieldsWhenAddedToManager() {
        Task originalTask = new Task("Original", "Description", Status.NEW);
        taskManager.createTask(originalTask);

        assertTrue(originalTask.getId() > 0, "Задаче должен быть назначен ID");

        Task savedTask = taskManager.getTaskById(originalTask.getId());
        assertEquals(originalTask.getName(), savedTask.getName(), "Имя задачи изменилось");
        assertEquals(originalTask.getDescription(), savedTask.getDescription(),
                "Описание задачи изменилось");
        assertEquals(originalTask.getStatus(), savedTask.getStatus(), "Статус задачи изменился");
    }
}
package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task("Test task", "Test description", Status.NEW);
        int taskId = taskManager.createTask(task);
        Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task.getName(), savedTask.getName(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(Status.NEW, savedTask.getStatus(), "Статус задачи должен быть NEW");
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic.getName(), savedEpic.getName(), "Название эпика не совпадает");
        assertEquals(epic.getDescription(), savedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void shouldCreateAndGetSubtask() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask.getName(), savedSubtask.getName(), "Название подзадачи не совпадает");
        assertEquals(subtask.getDescription(), savedSubtask.getDescription(), "Описание подзадачи не совпадает");
        assertEquals(Status.NEW, savedSubtask.getStatus(), "Статус подзадачи должен быть NEW");
        assertEquals(epicId, savedSubtask.getEpicId(), "ID эпика не совпадает");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Test task", "Test description", Status.IN_PROGRESS);
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task(taskId, "Updated task", "Updated description", Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals("Updated task", savedTask.getName(), "Название задачи не обновлено");
        assertEquals("Updated description", savedTask.getDescription(), "Описание задачи не обновлено");
        assertEquals(Status.IN_PROGRESS, savedTask.getStatus(), "Статус задачи не обновлен");
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Test task", "Test description", Status.DONE);
        int taskId = taskManager.createTask(task);

        taskManager.deleteTaskById(taskId);
        assertNull(taskManager.getTaskById(taskId), "Задача не удалена");
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.DONE, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.deleteEpicById(epicId);
        assertNull(taskManager.getEpicById(epicId), "Эпик не удален");
        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача не удалена");
    }

    @Test
    void shouldDeleteSubtask() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.DONE, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.deleteSubtaskById(subtaskId);
        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача не удалена");
    }

    @Test
    void shouldGetPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Description", Status.NEW, Duration.ofMinutes(30), now.plusHours(1));
        Task task2 = new Task("Task 2", "Description", Status.NEW, Duration.ofMinutes(45), now);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Set<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size(), "Неверное количество задач в списке приоритетов");
        assertEquals(task2, prioritized.iterator().next(), "Первой должна быть задача с более ранним временем начала");
    }

    @Test
    void shouldNotAllowTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Description", Status.NEW, Duration.ofHours(1), now);
        taskManager.createTask(task1);

        Task overlappingTask = new Task("Task 2", "Description", Status.NEW, Duration.ofHours(1), now.plusMinutes(30));
        assertThrows(ManagerSaveException.class, () -> taskManager.createTask(overlappingTask),
                "Должно быть исключение при пересечении времени задач");
    }

    @Test
    void shouldCalculateEpicStatus() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        // Все подзадачи NEW
        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epicId);
        taskManager.createSubtask(subtask1);
        assertEquals(Status.NEW, taskManager.getEpicById(epicId).getStatus(), "Статус должен быть NEW");

        // Одна подзадача DONE
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.DONE, taskManager.getEpicById(epicId).getStatus(), "Статус должен быть DONE");

        // Все подзадачи DONE
        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.DONE, epicId);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.DONE, taskManager.getEpicById(epicId).getStatus(), "Статус должен быть DONE");
    }
}
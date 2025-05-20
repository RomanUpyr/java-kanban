package manager;

import model.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    public void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    public void testCreateAndGetTask() {
        Task task = new Task("Test task", "Test description", Status.NEW);
        int taskId = taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");

        Collection<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals(task, tasks.get(1), "Задачи не совпадают");
    }

    @Test
    public void testCreateAndGetEpic() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика должен быть NEW");

        Collection<Task> epics = taskManager.getAllEpics();
        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(1, epics.size(), "Неверное количество эпиков");
        assertEquals(epic, epics.get(0), "Эпики не совпадают");
    }

    @Test
    public void testCreateAndGetSubtask() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");

        Collection<Task> subtasks = taskManager.getAllSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают");

        List<Subtask> epicSubtasks = taskManager.getSubtasksByEpicId(epicId);
        assertNotNull(epicSubtasks, "Подзадачи эпика не возвращаются");
        assertEquals(1, epicSubtasks.size(), "Неверное количество подзадач у эпика");
        assertEquals(subtask, epicSubtasks.get(0), "Подзадачи не совпадают");
    }

    @Test
    public void testUpdateTask() {
        Task task = new Task("Test task", "Test description", Status.NEW);
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task(taskId, "Updated task", "Updated description", Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals(updatedTask, savedTask, "Задача не обновлена");
    }

    @Test
    public void testUpdateEpic() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Epic updatedEpic = new Epic(epicId, "Updated epic", "Updated description");
        taskManager.updateEpic(updatedEpic);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(updatedEpic, savedEpic, "Эпик не обновлен");
    }

    @Test
    public void testUpdateSubtask() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask(subtaskId, "Updated subtask", "Updated description",
                Status.IN_PROGRESS, epicId);
        taskManager.updateSubtask(updatedSubtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertEquals(updatedSubtask, savedSubtask, "Подзадача не обновлена");
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task("Test task", "Test description", Status.NEW);
        int taskId = taskManager.createTask(task);

        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId), "Задача не удалена");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    public void testDeleteEpic() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getEpicById(epicId), "Эпик не удален");
        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача не удалена");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    public void testDeleteSubtask() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test subtask", "Test description", Status.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача не удалена");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "Подзадача должна быть удалена из эпика");
    }

    @Test
    public void testDeleteAllTasks() {
        Task task1 = new Task("Test task 1", "Test description", Status.NEW);
        Task task2 = new Task("Test task 2", "Test description", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    public void testDeleteAllEpics() {
        Epic epic1 = new Epic("Test epic 1", "Test description");
        Epic epic2 = new Epic("Test epic 2", "Test description");
        int epic1Id = taskManager.createEpic(epic1);
        int epic2Id = taskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.NEW, epic2Id);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getAllEpics().isEmpty(), "Все эпики должны быть удалены");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
    }

    @Test
    public void testDeleteAllSubtasks() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.NEW, epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        taskManager.deleteAllSubtasks();

        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены");

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "Все подзадачи должны быть удалены из эпика");
    }

    @Test
    public void testGetHistory() {
        Task task = new Task("Test task", "Test description", Status.NEW);
        Epic epic = new Epic("Test epic", "Test description");

        int taskId = taskManager.createTask(task);
        int epicId = taskManager.createEpic(epic);

        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(task, history.get(0), "Первая задача в истории не совпадает");
        assertEquals(epic, history.get(1), "Вторая задача в истории не совпадает");
    }

    @Test
    public void testEpicStatusAllSubtasksNew() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.NEW, epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    public void testEpicStatusAllSubtasksDone() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.DONE, epicId);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.DONE, epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(Status.DONE, savedEpic.getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    public void testEpicStatusSubtasksNewAndDone() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.DONE, epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    public void testEpicStatusAllSubtasksInProgress() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.IN_PROGRESS, epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    public void testEpicStatusNoSubtasks() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    public void testTaskTimeFields() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);

        Task task = new Task("Test task", "Test description", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals(startTime, savedTask.getStartTime(), "Время начала не совпадает");
        assertEquals(duration, savedTask.getDuration(), "Продолжительность не совпадает");
        assertEquals(startTime.plus(duration), savedTask.getEndTime(), "Время окончания не совпадает");
    }

    @Test
    public void testEpicTimeFields() {
        Epic epic = new Epic("Test epic", "Test description");
        int epicId = taskManager.createEpic(epic);

        LocalDateTime subtask1Start = LocalDateTime.now();
        Duration subtask1Duration = Duration.ofMinutes(30);
        Subtask subtask1 = new Subtask("Test subtask 1", "Test description", Status.NEW,
                epicId, subtask1Start, subtask1Duration);
        taskManager.createSubtask(subtask1);

        LocalDateTime subtask2Start = subtask1Start.plusHours(1);
        Duration subtask2Duration = Duration.ofMinutes(45);
        Subtask subtask2 = new Subtask("Test subtask 2", "Test description", Status.NEW,
                epicId, subtask2Start, subtask2Duration);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(subtask1Start, savedEpic.getStartTime(), "Время начала эпика не совпадает");
        assertEquals(subtask1Duration.plus(subtask2Duration), savedEpic.getDuration(),
                "Продолжительность эпика не совпадает");
        assertEquals(subtask2.getEndTime(), savedEpic.getEndTime(), "Время окончания эпика не совпадает");
    }

    @Test
    public void testGetPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Status.NEW, now.plusHours(2), Duration.ofMinutes(30));
        Task task2 = new Task("Task 2", "Description", Status.NEW, now, Duration.ofMinutes(45));
        Task task3 = new Task("Task 3", "Description", Status.NEW, now.plusHours(1), Duration.ofMinutes(15));

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        List<Task> prioritized = List.copyOf(taskManager.getPrioritizedTasks());
        assertEquals(3, prioritized.size(), "Неверное количество задач");
        assertEquals(task2, prioritized.get(0), "Первая задача должна быть task2");
        assertEquals(task3, prioritized.get(1), "Вторая задача должна быть task3");
        assertEquals(task1, prioritized.get(2), "Третья задача должна быть task1");
    }

    @Test
    public void testTasksWithoutTimeNotInPrioritized() {
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        Task task2 = new Task("Task 2", "Description", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Set<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size(), "Только задачи с временем должны быть в списке");
        assertTrue(prioritized.contains(task2), "Задача с временем должна быть в списке");
    }

    @Test
    public void testTasksOverlapDetection() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Status.NEW, now, Duration.ofMinutes(30));
        taskManager.createTask(task1);

        // Пересекающаяся задача
        Task overlappingTask = new Task("Task 2", "Description", Status.NEW,
                now.plusMinutes(15), Duration.ofMinutes(30));

        assertThrows(ManagerSaveException.class, () -> taskManager.createTask(overlappingTask),
                "Должно быть исключение при пересечении задач");
    }

    @Test
    public void testNonOverlappingTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Status.NEW, now, Duration.ofMinutes(30));
        taskManager.createTask(task1);

        // Непересекающаяся задача
        Task nonOverlappingTask = new Task("Task 2", "Description", Status.NEW,
                now.plusHours(1), Duration.ofMinutes(30));

        assertDoesNotThrow(() -> taskManager.createTask(nonOverlappingTask),
                "Не должно быть исключения для непересекающихся задач");
    }
}
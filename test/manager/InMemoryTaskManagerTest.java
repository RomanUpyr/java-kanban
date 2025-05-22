package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    void additionalSetUp() {
        // Дополнительная инициализация, если нужна
    }

    @Test
    void shouldNotAddTaskWithoutTimeToPrioritizedList() {
        Task task = new Task("Task without time", "Description", Status.NEW);
        taskManager.createTask(task);

        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void shouldRemoveTaskFromPrioritizedListWhenDeleted() {
        Task task = new Task("Task", "Description",
                Status.NEW, Duration.ofHours(1),
                LocalDateTime.now());
        int taskId = taskManager.createTask(task);
        taskManager.deleteTaskById(taskId);

        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void shouldUpdateTaskInPrioritizedList() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task", "Description",
                Status.NEW, Duration.ofHours(1),
                now);
        int taskId = taskManager.createTask(task);

        Task updated = new Task(taskId, "Updated", "Desc",
                Status.IN_PROGRESS, Duration.ofHours(2),
                now.plusHours(1));
        taskManager.updateTask(updated);

        Set<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals(now.plusHours(1), prioritized.iterator().next().getStartTime());
    }

    @Test
    void shouldHandleEmptyEpicTime() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertNull(savedEpic.getStartTime());
        assertNull(savedEpic.getDuration());
        assertNull(savedEpic.getEndTime());
    }
}
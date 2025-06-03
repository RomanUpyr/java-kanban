package manager;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(task1, history.get(0), "Первая задача в истории не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача в истории не совпадает");
    }

    @Test
    void shouldNotAddDuplicateTasks() {
        Task task = new Task(1, "Task", "Description", Status.NEW);

        historyManager.add(task);
        historyManager.add(task); // Добавляем ту же задачу второй раз

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дубликаты не должны добавляться в историю");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не была удалена из истории");
        assertEquals(task2, history.get(0), "Оставшаяся задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromMiddleOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);
        Task task3 = new Task(3, "Task 3", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Последняя задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromHeadOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Неверное количество задач после удаления головы");
        assertEquals(task2, history.get(0), "Новая голова не совпадает");
    }

    @Test
    void shouldRemoveTaskFromTailOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Неверное количество задач после удаления хвоста");
        assertEquals(task1, history.get(0), "Хвост не обновился корректно");
    }

    @Test
    void shouldPreserveTaskFieldsInHistory() {
        Task originalTask = new Task(1, "Original", "Description", Status.NEW);
        historyManager.add(originalTask);

        Task historyTask = historyManager.getHistory().get(0);
        assertEquals(originalTask.getName(), historyTask.getName(), "Имя задачи в истории изменилось");
        assertEquals(originalTask.getDescription(), historyTask.getDescription(),
                "Описание задачи в истории изменилось");
        assertEquals(originalTask.getStatus(), historyTask.getStatus(),
                "Статус задачи в истории изменился");
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой при инициализации");
    }

    @Test
    void shouldNotFailWhenRemovingNonExistentTask() {
        historyManager.remove(999); // Попытка удалить несуществующую задачу
        // Ожидаем, что исключения не будет
    }

    @Test
    void shouldMaintainOrderAfterMultipleOperations() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(2, "Task 2", "Description", Status.NEW);
        Task task3 = new Task(3, "Task 3", "Description", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Неверное количество задач после операций");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Вторая задача не совпадает");
        assertEquals(task2, history.get(2), "Третья задача не совпадает");
    }
}
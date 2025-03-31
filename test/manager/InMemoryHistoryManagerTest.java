package manager;

import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void shouldAddTasksToHistory() { // Проверяем добавление задач в историю
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
    void shouldLimitHistorySize() { // Проверяем ограничение размера истории
        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Description", Status.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна быть ограничена 10 задачами");
        assertEquals(6, history.get(0).getId(), "Первая задача должна быть 6-й (старые удаляются)");
        assertEquals(15, history.get(9).getId(), "Последняя задача должна быть 15-й");
    }

    @Test
    void shouldPreserveTaskFieldsInHistory() {
        /* Проверяем, что при добавлении задачи в историю просмотров все её основные поля
        (название, описание, статус) сохраняются без изменений.
         */
        Task originalTask = new Task(1, "Original", "Description", Status.NEW);
        historyManager.add(originalTask);

        Task historyTask = historyManager.getHistory().get(0);
        assertEquals(originalTask.getName(), historyTask.getName(), "Имя задачи в истории изменилось");
        assertEquals(originalTask.getDescription(), historyTask.getDescription(),
                "Описание задачи в истории изменилось");
        assertEquals(originalTask.getStatus(), historyTask.getStatus(),
                "Статус задачи в истории изменился");
    }
}
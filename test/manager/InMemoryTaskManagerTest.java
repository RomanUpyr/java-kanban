package manager;

import model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager = Managers.getDefault();

    @Test
    void shouldAddAndFindTaskById() { // Проверяем добавление и поиск по id
        Task task = new Task(1,"Task", "Description", Status.NEW);
        taskManager.createTask(task);
        final int taskId = task.getId();

        Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldAddAndFindEpicById() { // Проверяем добавление и поиск по id
        Epic epic = new Epic(1,"Epic", "Description");
        taskManager.createEpic(epic);
        final int epicId = epic.getId();

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
    }

    @Test
    void shouldAddAndFindSubtaskById() { // Проверяем добавление и поиск по id
        Epic epic = new Epic(1,"Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(2, "Subtask", "Description", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);
        final int subtaskId = subtask.getId();

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");
    }

    @Test
    void shouldNotConflictTasksWithGeneratedAndProvidedIds() { // Проверяем отсутствие конфликтов между задачами с заданным и сгенерированным id
        // Создаем задачу с явно указанным ID
        Task taskWithId = new Task(1, "Task with id", "Description", Status.NEW);
        taskManager.createTask(taskWithId);
        // Создаем задачу с автоматической генерацией ID
        Task taskWithGeneratedId = new Task(0,"Task without id", "Description", Status.NEW);
        taskManager.createTask(taskWithGeneratedId);

        assertNotEquals(taskWithId.getId(), taskWithGeneratedId.getId(), "ID задач не должны конфликтовать");
        assertTrue(taskWithGeneratedId.getId() > 0, "Должен быть установлен валидный ID");
    }

    @Test
    void shouldNotAddSubtaskToItselfAsEpic() { // Проверяем, что нельзя добавить подзадачу в саму себя как эпик
        Subtask subtask = new Subtask(1,"Subtask", "Description", Status.NEW, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubtask(subtask);
        }, "Нельзя добавить подзадачу в саму себя как эпик");
    }

    @Test
    void shouldUpdateEpicStatusBasedOnSubtasks() { // Проверяем автоматическое обновление статуса эпика на основе статусов подзадач
        Epic epic = new Epic(0,"Epic", "Description");
        taskManager.createEpic(epic);

        // Статус должен быть NEW при создании
        assertEquals(Status.NEW, epic.getStatus(), "Новый эпик должен иметь статус NEW");

        // Добавляем подзадачу со статусом NEW
        Subtask subtask1 = new Subtask(0,"Subtask 1", "Description", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        assertEquals(Status.NEW, epic.getStatus(), "Эпик с NEW подзадачами должен иметь статус NEW");

        // Добавляем подзадачу со статусом DONE
        Subtask subtask2 = new Subtask(0,"Subtask 2", "Description", Status.DONE, epic.getId());
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
    void shouldNotAddEpicToItselfAsSubtask() {
        /*
        Проверяем, что система корректно обрабатывает попытку добавить эпик в качестве подзадачи самого себя.
         */
        Epic epic = new Epic(1,"Epic", "Description");
        taskManager.createEpic(epic);

        // Пытаемся создать подзадачу с id эпика
        Subtask subtask = new Subtask(1,"Subtask", "Description", Status.NEW, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubtask(subtask);
        }, "Нельзя добавить эпик в самого себя как подзадачу");
    }

    @Test
    void shouldPreserveTaskFieldsWhenAddedToManager() {
        /*
        Проверяем, что при добавлении задачи в менеджер все поля сохраняются без изменений
         */
        Task originalTask = new Task(0,"Original", "Description", Status.NEW);
        taskManager.createTask(originalTask);
        // Проверяем, что менеджер назначил ID
        assertTrue(originalTask.getId() > 0, "Задаче должен быть назначен ID");

        Task savedTask = taskManager.getTaskById(originalTask.getId());
        assertEquals(originalTask.getName(), savedTask.getName(), "Имя задачи изменилось");
        assertEquals(originalTask.getDescription(), savedTask.getDescription(),
                "Описание задачи изменилось");
        assertEquals(originalTask.getStatus(), savedTask.getStatus(), "Статус задачи изменился");
    }
}
package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertTrue(loadedManager.getAllTasks().isEmpty()),
                () -> assertTrue(loadedManager.getAllSubtasks().isEmpty()),
                () -> assertTrue(loadedManager.getAllEpics().isEmpty())
        );
    }

    @Test
    void testSaveAndLoadTasks() {
        // Создаем тестовые данные с явными ID
        Task task = new Task(1, "Test Task", "Description", Status.NEW);
        manager.createTask(task);

        Epic epic = new Epic(2, "Test Epic", "Epic Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask(3, "Test Subtask", "Sub Desc", Status.DONE, epic.getId());
        manager.createSubtask(subtask);

        // Загружаем данные из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем все условия одним assertAll
        assertAll(
                () -> assertEquals(1, loadedManager.getAllTasks().size(),
                        "Должна быть 1 задача"),
                () -> assertEquals(task, loadedManager.getTaskById(task.getId()),
                        "Загруженная задача должна соответствовать сохраненной"),
                () -> assertEquals(1, loadedManager.getAllEpics().size(),
                        "Должен быть 1 эпик"),
                () -> assertEquals(epic, loadedManager.getEpicById(epic.getId()),
                        "Загруженный эпик должен соответствовать сохраненному"),
                () -> assertEquals(1, loadedManager.getAllSubtasks().size(),
                        "Должна быть 1 подзадача"),
                () -> assertEquals(subtask, loadedManager.getSubtaskById(subtask.getId()),
                        "Загруженная подзадача должна соответствовать сохраненной"),
                () -> assertEquals(1, loadedManager.getSubtasksByEpicId(epic.getId()).size(),
                        "У эпика должна быть 1 подзадача")
        );
    }

    @Test
    void testTaskFieldsAfterLoading() {
        Task task = new Task(1, "Task", "Desc", Status.IN_PROGRESS);
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTaskById(task.getId());

        assertAll(
                () -> assertEquals(task.getName(), loadedTask.getName()),
                () -> assertEquals(task.getDescription(), loadedTask.getDescription()),
                () -> assertEquals(task.getStatus(), loadedTask.getStatus()),
                () -> assertEquals(task.getId(), loadedTask.getId())
        );
    }

    @Test
    void testEpicStatusAfterLoading() {
        Epic epic = new Epic(1, "Epic", "Epic Desc");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask(2, "Sub 1", "Desc", Status.DONE, epic.getId());
        Subtask subtask2 = new Subtask(3, "Sub 2", "Desc", Status.IN_PROGRESS, epic.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());

        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus());
    }

    @Test
    void testHistoryAfterLoading() {
        Task task = new Task(1, "Task", "Desc", Status.NEW);
        Epic epic = new Epic(2, "Epic", "Desc");

        manager.createTask(task);
        manager.createEpic(epic);

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> history = loadedManager.getHistory();

        assertAll(
                () -> assertEquals(2, history.size()),
                () -> assertTrue(history.contains(task)),
                () -> assertTrue(history.contains(epic))
        );
    }
}
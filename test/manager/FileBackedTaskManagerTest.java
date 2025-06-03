package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager taskManager;

    /**
     * Инициализация перед каждым тестом:
     * - создаем временный файл
     * - инициализируем менеджер
     */
    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    /**
     * Очистка после каждого теста:
     * - удаляем временный файл
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    /**
     * Тест сохранения и загрузки пустого менеджера
     */
    @Test
    void testSaveAndLoadEmptyManager() {
        // Сохраняем состояние
        taskManager.save();

        // Проверяем что файл содержит только заголовок и разделитель
        assertTrue(tempFile.length() > 0, "Файл должен содержать заголовок");

        // Загружаем состояние
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем что все коллекции пусты
        assertAll(
                () -> assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пуст"),
                () -> assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пуст"),
                () -> assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пуст"),
                () -> assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пуста")
        );
    }

    /**
     * Тест корректности формата сохраняемого файла
     */
    @Test
    void testFileFormat() throws IOException {
        // Создаем тестовые данные
        Task task = new Task("Task", "Description", Status.NEW);
        Epic epic = new Epic("Epic", "Epic description");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Sub description", Status.DONE, epic.getId());

        taskManager.createTask(task);
        taskManager.createSubtask(subtask);

        // Читаем файл
        List<String> lines = Files.readAllLines(tempFile.toPath());

        // Проверяем заголовок
        assertEquals("id,type,name,status,description,epic", lines.get(0), "Некорректный заголовок");

        // Проверяем формат задач
        assertTrue(lines.get(1).matches("\\d+,TASK,Task,NEW,Description,"), "Некорректный формат задачи");
        assertTrue(lines.get(3).matches("\\d+,SUBTASK,Subtask,DONE,Sub description,\\d+"),
                "Некорректный формат подзадачи");

        // Проверяем разделитель
        assertEquals("", lines.get(4), "Отсутствует разделитель перед историей");
    }

    /**
     * Тест восстановления всех типов задач с проверкой полей
     */
    @Test
    void testLoadTasksWithAllFields() {
        // Создаем тестовые данные
        Task task = new Task("Task", "Description", Status.IN_PROGRESS);
        Epic epic = new Epic("Epic", "Epic desc");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Sub desc", Status.DONE, epic.getId());

        taskManager.createTask(task);
        taskManager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем восстановленные задачи
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertAll(
                () -> assertEquals(task.getName(), loadedTask.getName(), "Не совпадает имя задачи"),
                () -> assertEquals(task.getDescription(), loadedTask.getDescription(), "Не совпадает описание"),
                () -> assertEquals(task.getStatus(), loadedTask.getStatus(), "Не совпадает статус")
        );

        // Проверяем подзадачи
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        assertAll(
                () -> assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(), "Не совпадает EpicId"),
                () -> assertEquals(epic.getId(), loadedSubtask.getEpicId(), "Неверная связь с эпиком")
        );
    }

    /**
     * Тест сохранения и восстановления истории просмотров
     */
    @Test
    void testHistoryPreservation() {
        // Создаем задачи
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Формируем историю в определенном порядке
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());

        // Явно сохраняем состояние (на всякий случай)
        taskManager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем историю
        List<Task> history = loadedManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");

        // Дополнительные проверки порядка
        if (history.size() == 2) {
            assertEquals(task2.getId(), history.get(0).getId(), "Нарушен порядок истории");
            assertEquals(task1.getId(), history.get(1).getId(), "Нарушен порядок истории");
        }
    }

    /**
     * Тест загрузки из несуществующего файла
     */
    @Test
    void testLoadFromNonExistentFile(@TempDir Path tempDir) {
        File nonExistentFile = tempDir.resolve("nonexistent.csv").toFile();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(nonExistentFile);

        // Проверяем что менеджер создан и коллекции пусты
        assertNotNull(manager, "Менеджер должен быть создан");
        assertTrue(manager.getAllTasks().isEmpty(), "Задачи должны быть пусты");
    }

    /**
     * Тест работы с пустой историей просмотров
     */
    @Test
    void testEmptyHistory() {
        // Создаем задачу без добавления в историю
        Task task = new Task("Task", "Desc", Status.NEW);
        taskManager.createTask(task);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем что задача есть, а истории нет
        assertAll(
                () -> assertEquals(1, loadedManager.getAllTasks().size(), "Должна быть одна задача"),
                () -> assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пуста")
        );
    }


    /**
     * Тест удаления задач с проверкой сохранения в файл
     */
    @Test
    void testDeleteTasks() {
        // Создаем тестовые данные
        Task task = new Task("Task", "Desc", Status.NEW);
        taskManager.createTask(task);

        // Удаляем задачу
        taskManager.deleteTaskById(task.getId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем что задача удалена
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задача должна быть удалена");
    }

    /**
     * Тест обновления задач с проверкой сохранения изменений
     */
    @Test
    void testUpdateTask() {
        // Создаем и обновляем задачу
        Task task = new Task("Original", "Desc", Status.NEW);
        taskManager.createTask(task);

        task.setName("Updated");
        task.setStatus(Status.DONE);
        taskManager.updateTask(task);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTaskById(task.getId());

        // Проверяем обновленные поля
        assertAll(
                () -> assertEquals("Updated", loadedTask.getName(), "Имя не обновилось"),
                () -> assertEquals(Status.DONE, loadedTask.getStatus(), "Статус не обновился")
        );
    }
}
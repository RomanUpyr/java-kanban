package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File testFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        testFile = File.createTempFile("test_tasks", ".csv");
        manager = new FileBackedTaskManager(testFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFile.toPath());
    }

    @Test
    @DisplayName("Сохранение и загрузка пустого менеджера")
    void shouldSaveAndLoadEmptyManager() {
        manager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertTrue(loaded.getAllTasks().isEmpty(), "Список задач должен быть пустым"),
                () -> assertTrue(loaded.getAllEpics().isEmpty(), "Список эпиков должен быть пустым"),
                () -> assertTrue(loaded.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым"),
                () -> assertTrue(loaded.getHistory().isEmpty(), "История должна быть пустой")
        );
    }

    @Test
    @DisplayName("Сохранение и загрузка задач")
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Тест задача", "Описание", Status.IN_PROGRESS);
        int taskId = manager.createTask(task);

        Epic epic = new Epic("Тест эпик", "Описание");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Тест подзадача", "Описание", Status.IN_PROGRESS, epicId);
        int subtaskId = manager.createSubtask(subtask);

        manager.getTaskById(taskId); // Добавляем в историю
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertEquals(1, loaded.getAllTasks().size(), "Должна быть 1 задача"),
                () -> assertEquals(1, loaded.getAllEpics().size(), "Должен быть 1 эпик"),
                () -> assertEquals(1, loaded.getAllSubtasks().size(), "Должна быть 1 подзадача"),
                () -> assertEquals(1, loaded.getHistory().size(), "Должна быть 1 задача в истории"),
                () -> assertEquals(taskId, loaded.getTaskById(taskId).getId(), "ID задачи должен совпадать"),
                () -> assertEquals(epicId, loaded.getEpicById(epicId).getId(), "ID эпика должен совпадать"),
                () -> assertEquals(subtaskId, loaded.getSubtaskById(subtaskId).getId(), "ID подзадачи должен совпадать")
        );
    }

    @Test
    @DisplayName("Сохранение и загрузка времени выполнения")
    void shouldSaveAndLoadTime() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.toFile());

        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(1);
        Task task = new Task("Test", "Description", Status.NEW, duration, startTime);
        manager.createTask(task);

        String fileContent = Files.readString(tempFile);
        System.out.println("File content:\n" + fileContent);

        // Проверяем оба формата времени
        String expectedTime1 = "01.01.2023 10:00";
        String expectedTime2 = "2023-01-01T10:00";
        assertTrue(fileContent.contains(expectedTime1) || fileContent.contains(expectedTime2),
                "Expected time not found in:\n" + fileContent);

        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("Обработка невалидного файла")
    void shouldThrowWhenFileInvalid() {
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(new File("/invalid/path"));
            manager.createTask(new Task(1, "Test", "Description", Status.NEW));
            manager.save();
        }, "Должно выбрасываться исключение при работе с невалидным файлом");
    }

    @Test
    @DisplayName("Обработка пустого файла")
    void shouldHandleEmptyFile() throws IOException {
        Files.write(testFile.toPath(), new byte[0]);
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(testFile),
                "Должен создаваться пустой менеджер при загрузке пустого файла");
    }
}
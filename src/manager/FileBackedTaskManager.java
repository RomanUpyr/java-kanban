package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FileBackedTaskManager расширяет InMemoryTaskManager, добавляя функциональность сохранения в файл.
 * Автоматически сохраняет состояние задач в указанный файл после каждого изменения.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TEST_FORMATTER = // Добавлен для тестов
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Конструктор создает новый FileBackedTaskManager, использующий указанный файл для хранения.
     *
     * @param file файл для сохранения данных задач
     * @throws ManagerLoadException если файл равен null
     */
    public FileBackedTaskManager(File file) {
        if (file == null) {
            throw new ManagerLoadException("Файл не может быть null");
        }
        this.file = file;
    }

    /**
     * Собственное непроверяемое исключение для ошибок загрузки данных
     */
    public static class ManagerLoadException extends RuntimeException {
        public ManagerLoadException(String message) {
            super(message);
        }

        public ManagerLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Статический метод для создания FileBackedTaskManager из сохраненного файла.
     * Восстанавливает состояние менеджера из указанного файла.
     *
     * @param file файл с сохраненными данными задач
     * @return новый FileBackedTaskManager с восстановленным состоянием
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        if (!file.exists()) {
            throw new ManagerSaveException("Файл не существует: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new ManagerSaveException("Невозможно прочитать файл: " + file.getPath());
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    /**
     * Сохраняет текущее состояние всех задач в файл в формате CSV.
     *
     * @throws ManagerSaveException при ошибках записи в файл
     */
    protected void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,type,name,status,description,epic,duration,startTime");

            // Сериализация задач
            for (Task task : tasks.values()) {
                lines.add(taskToString(task));
            }
            for (Epic epic : epics.values()) {
                lines.add(taskToString(epic));
            }
            for (Subtask subtask : subtasks.values()) {
                lines.add(taskToString(subtask));
            }

            lines.add("");
            lines.add(historyToString(historyManager));

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Save failed", e);
        }
    }

    private String serializeTask(Task task) {
        return String.join(",",
                String.valueOf(task.getId()),
                "TASK",
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                "", // Пустое поле для эпика
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "",
                task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : ""
        );
    }

    /**
     * Загружает задачи из файла и восстанавливает состояние менеджера.
     *
     * @throws ManagerLoadException при ошибках чтения файла
     */
    private void load() {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return;

            // Разделяем задачи и историю
            int separatorIndex = lines.indexOf("");
            List<String> taskLines = separatorIndex == -1
                    ? lines
                    : lines.subList(0, separatorIndex);

            // Пропускаем заголовок и обрабатываем задачи
            for (int i = 1; i < taskLines.size(); i++) {
                String line = taskLines.get(i).trim();
                if (!line.isEmpty()) {
                    Task task = fromString(line);
                    if (task != null) {
                        addRestoredTask(task);
                    }
                }
            }

            // Восстанавливаем историю просмотров
            if (separatorIndex != -1 && separatorIndex + 1 < lines.size()) {
                String historyLine = lines.get(separatorIndex + 1).trim();
                if (!historyLine.isEmpty()) {
                    restoreHistory(historyLine);
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка чтения из файла", e);
        }
    }

    /**
     * Добавляет восстановленную задачу в соответствующую коллекцию.
     *
     * @param task задача для восстановления
     * @throws IllegalArgumentException если данные задачи некорректны
     */
    private void addRestoredTask(Task task) {
        if (tasks.containsKey(task.getId()) || epics.containsKey(task.getId()) || subtasks.containsKey(task.getId())) {
            throw new ManagerLoadException("Задача с ID " + task.getId() + " уже существует");
        }
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            epics.put(epic.getId(), epic);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            validateSubtask(subtask);
            subtasks.put(subtask.getId(), subtask);
            epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        } else {
            tasks.put(task.getId(), task);
        }

        // Обновляем счетчик ID
        if (nextId <= task.getId()) {
            nextId = task.getId() + 1;
        }
    }

    /**
     * Исключение для ошибок валидации подзадач
     */
    public static class SubtaskValidationException extends RuntimeException {
        public SubtaskValidationException(String message) {
            super(message);
        }
    }

    /**
     * Проверяет корректность подзадачи перед добавлением.
     *
     * @param subtask подзадача для проверки
     * @throws SubtaskValidationException если данные подзадачи некорректны
     */
    private void validateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new SubtaskValidationException("Подзадача не может быть null");
        }

        int subtaskId = subtask.getId();
        int epicId = subtask.getEpicId();

        // Проверяем, что подзадача не ссылается на саму себя как на эпик
        if (subtaskId == epicId) {
            throw new SubtaskValidationException(
                    "Подзадача " + subtaskId + " не может ссылаться на саму себя как на эпик");
        }

        // Проверяем существование эпика
        if (!epics.containsKey(epicId)) {
            throw new SubtaskValidationException(
                    "Эпик " + epicId + " не найден для подзадачи " + subtaskId);
        }
    }

    /**
     * Преобразует задачу в строку CSV.
     *
     * @param task задача для преобразования
     * @return строка в формате CSV
     */
    private String taskToString(Task task) {
        List<String> fields = new ArrayList<>();
        fields.add(String.valueOf(task.getId()));
        fields.add(getTaskType(task));
        fields.add(task.getName());
        fields.add(task.getStatus().name());
        fields.add(task.getDescription());

        if (task instanceof Epic) {
            fields.add(((Epic) task).getSubtaskIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(";")));
        } else if (task instanceof Subtask) {
            fields.add(String.valueOf(((Subtask) task).getEpicId()));
        } else {
            fields.add("");
        }

        fields.add(task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "");
        fields.add(task.getStartTime() != null ?
                task.getStartTime().format(DATE_TIME_FORMATTER) : "");

        return String.join(",", fields);
    }

    /**
     * Определяет тип задачи для сохранения.
     *
     * @param task задача для определения типа
     * @return строковое представление типа задачи
     */
    private String getTaskType(Task task) {
        if (task instanceof Epic) {
            return "EPIC";
        } else if (task instanceof Subtask) {
            return "SUBTASK";
        } else {
            return "TASK";
        }
    }

    /**
     * Преобразует историю просмотров в строку.
     *
     * @param manager менеджер истории
     * @return строка с ID задач через запятую
     */
    private static String historyToString(HistoryManager manager) {
        StringBuilder result = new StringBuilder();
        List<Task> history = manager.getHistory();

        for (int i = 0; i < history.size(); i++) {
            result.append(history.get(i).getId());
            if (i < history.size() - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }

    /**
     * Создает задачу из строки CSV.
     *
     * @param value строка CSV
     * @return восстановленная задача
     * @throws IllegalArgumentException если строка некорректна
     */
    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split(",", -1); // -1 сохраняет пустые значения
        if (parts.length < 8) {
            throw new IllegalArgumentException("Некорректная строка задачи: " + value);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String type = parts[1].trim();
            String name = parts[2].trim();
            Status status = Status.valueOf(parts[3].trim());
            String description = parts[4].trim();
            String subtasksIdsStr = parts[5].trim();

            // Парсим временные параметры
            Duration duration = parts[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[6]));
            LocalDateTime startTime = parts[7].isEmpty() ? null :
                    LocalDateTime.parse(parts[7], DATE_TIME_FORMATTER);

            switch (type) {
                case "TASK":
                    return new Task(id, name, description, status, duration, startTime);

                case "EPIC":
                    Epic epic = new Epic(id, name, description);
                    epic.setStatus(status);

                    // Добавляем подзадачи, если они есть
                    if (!subtasksIdsStr.isEmpty()) {
                        Arrays.stream(subtasksIdsStr.split(";"))
                                .filter(idStr -> !idStr.isEmpty())
                                .map(Integer::parseInt)
                                .forEach(epic::addSubtaskId);
                    }

                    // Устанавливаем временные параметры
                    epic.setStartTime(startTime);
                    epic.setDuration(duration);

                    return epic;

                case "SUBTASK":
                    if (parts[5].isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId: " + value);
                    }
                    int epicId = Integer.parseInt(parts[5].trim());
                    return new Subtask(id, name, description, status, epicId, duration, startTime);

                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга задачи из строки: " + value, e);
        }
    }

    /**
     * Восстанавливает историю просмотров из строки.
     *
     * @param historyData строка с ID задач через запятую
     */
    private void restoreHistory(String historyData) {
        if (historyData == null || historyData.isEmpty()) return;

        for (String id : historyData.split(",")) {
            try {
                int taskId = Integer.parseInt(id.trim());
                // Проверяем все хранилища задач
                Task task = tasks.get(taskId);
                if (task == null) task = epics.get(taskId);
                if (task == null) task = subtasks.get(taskId);
                if (task != null) {
                    historyManager.add(task);
                }
            } catch (NumberFormatException e) {
                System.err.println("Некорректный ID в истории: " + id);
            }
        }
    }

    // Переопределенные методы с сохранением состояния

    @Override
    public int createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask cannot be null");
        }
        validateSubtask(subtask);
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic cannot be null");
        }
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        validateSubtask(subtask);
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic newEpic) {
        // Проверки
        Objects.requireNonNull(newEpic, "Epic cannot be null");
        Epic existingEpic = epics.get(newEpic.getId());
        if (existingEpic == null) {
            throw new IllegalArgumentException("Epic not found");
        }

        // Создаем новый эпик с обновленными данными
        Epic updatedEpic = new Epic(newEpic.getId(),
                newEpic.getName(),
                newEpic.getDescription());

        // Копируем все данные из старого эпика
        updatedEpic.setStatus(existingEpic.getStatus());
        updatedEpic.getSubtaskIds().addAll(existingEpic.getSubtaskIds());
        updatedEpic.setStartTime(existingEpic.getStartTime());
        updatedEpic.setDuration(existingEpic.getDuration());

        // Полная замена в хранилище
        epics.remove(existingEpic.getId());
        epics.put(updatedEpic.getId(), updatedEpic);

        // Принудительное обновление файла
        try {
            save();
            System.out.println("DEBUG: Saved content:\n" + Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new ManagerSaveException("Save failed", e);
        }
    }


    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }
}
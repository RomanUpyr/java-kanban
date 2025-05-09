package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * FileBackedTaskManager расширяет InMemoryTaskManager, добавляя функциональность сохранения в файл.
 * Автоматически сохраняет состояние задач в указанный файл после каждого изменения.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;  // Файл для хранения задач

    /**
     * Конструктор создает новый FileBackedTaskManager, использующий указанный файл для хранения.
     *
     * @param file файл для сохранения данных задач
     * @throws IllegalArgumentException если файл равен null
     */
    public FileBackedTaskManager(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Файл не может быть null");
        }
        this.file = file;
    }

    /**
     * Статический метод для создания FileBackedTaskManager из сохраненного файла.
     * Восстанавливает состояние менеджера из указанного файла.
     *
     * @param file файл с сохраненными данными задач
     * @return новый FileBackedTaskManager с восстановленным состоянием
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (file.exists()) {
                manager.load();
            }
        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при загрузке из файла: " + e.getMessage());
        }
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
            lines.add("id,type,name,status,description,epic");  // Заголовок CSV

            // Сохраняем задачи всех типов
            tasks.values().forEach(task -> lines.add(taskToString(task)));
            epics.values().forEach(epic -> lines.add(taskToString(epic)));
            subtasks.values().forEach(subtask -> lines.add(taskToString(subtask)));

            // Сохраняем историю просмотров
            lines.add("");
            lines.add(historyToString(historyManager));

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    /**
     * Загружает задачи из файла и восстанавливает состояние менеджера.
     *
     * @throws ManagerSaveException при ошибках чтения файла
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
            throw new ManagerSaveException("Ошибка чтения из файла", e);
        }
    }

    /**
     * Добавляет восстановленную задачу в соответствующую коллекцию.
     *
     * @param task задача для восстановления
     * @throws IllegalArgumentException если данные задачи некорректны
     */
    private void addRestoredTask(Task task) {
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
     * Проверяет корректность подзадачи перед добавлением.
     *
     * @param subtask подзадача для проверки
     * @throws IllegalArgumentException если данные подзадачи некорректны
     */
    private void validateSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Подзадача " + subtask.getId() +
                    " не может ссылаться на саму себя как на эпик");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик " + subtask.getEpicId() +
                    " для подзадачи " + subtask.getId() + " не существует");
        }
    }

    /**
     * Преобразует задачу в строку CSV.
     *
     * @param task задача для преобразования
     * @return строка в формате CSV
     */
    private String taskToString(Task task) {
        String[] fields = new String[]{
                String.valueOf(task.getId()),
                getTaskType(task),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : ""
        };
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

        String[] parts = value.split(",", -1);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Некорректная строка задачи: " + value);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String type = parts[1].trim();
            String name = parts[2].trim();
            Status status = Status.valueOf(parts[3].trim());
            String description = parts[4].trim();

            switch (type) {
                case "TASK":
                    return new Task(id, name, description, status);
                case "EPIC":
                    Epic epic = new Epic(id, name, description);
                    epic.setStatus(status);
                    if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                        String[] subtaskIds = parts[5].trim().split(";");
                        for (String subtaskId : subtaskIds) {
                            if (!subtaskId.isEmpty()) {
                                epic.addSubtaskId(Integer.parseInt(subtaskId));
                            }
                        }
                    }
                    return epic;
                case "SUBTASK":
                    if (parts.length < 6 || parts[5].trim().isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId: " + value);
                    }
                    int epicId = Integer.parseInt(parts[5].trim());
                    return new Subtask(id, name, description, status, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный числовой формат в строке: " + value, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректное значение статуса в строке: " + value, e);
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
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        validateSubtask(subtask);
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
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
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
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
}
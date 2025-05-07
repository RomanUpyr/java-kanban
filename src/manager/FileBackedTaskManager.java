package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * FileBackedTaskManager расширяет InMemoryTaskManager, добавляя функциональность сохранения в файл.
 * Автоматически сохраняет состояние задач в указанный файл после каждого изменения.
 */

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;  // Файл для хранения задач

    /**
     * Конструктор создает новый FileBackedTaskManager, использующий указанный файл для хранения.
     *
     * @param file Передаем файл для сохранения данных задач
     */
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    /**
     * Статический метод для создания FileBackedTaskManager из сохраненного файла при запуске программы.
     * Восстанавливает состояние менеджера из указанного файла.
     *
     * @param file Файл с сохраненными данными задач
     * @return Новый FileBackedTaskManager с восстановленным состоянием
     * @throws ManagerSaveException при ошибках чтения файла
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        //Создаем новый пустой менеджер с указанным файлом
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    /* Добавляем восстановленные задачи в соответствующие коллекции
     * @param task задача для восстановления
     */

    private void addRestoredTask(Task task) {
        if (task instanceof Epic) {
            //Epic epic = (Epic) task;
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtaskId(subtask.getId());
            }
        } else {
            tasks.put(task.getId(), task);
        }

        // Обновляем nextId для избежания конфликтов ID
        if (nextId <= task.getId()) {
            nextId = task.getId() + 1;
        }
    }


    /**
     * Метод сохраняет текущее состояние всех задач в файл в формате CSV.
     *
     * @throws ManagerSaveException при ошибках записи в файл
     */
    protected void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,type,name,status,description,epic");

            // Сохраняем задачи
            tasks.values().forEach(task -> lines.add(toString(task)));
            epics.values().forEach(epic -> lines.add(toString(epic)));
            subtasks.values().forEach(subtask -> lines.add(toString(subtask)));

            // Сохраняем историю
            lines.add(""); // Пустая строка как разделитель
            lines.add(historyToString(historyManager));

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }
    private static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        return history.stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static List<Integer> historyFromString(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private void load() {
        try {
            String content = Files.readString(file.toPath());
            if (content.isEmpty()) {
                return;
            }

            String[] parts = content.split("\n\n", 2); // Разделяем задачи и историю
            String[] taskLines = parts[0].split("\n");

            // Пропускаем заголовок
            for (int i = 1; i < taskLines.length; i++) {
                Task task = fromString(taskLines[i]);
                if (task != null) {
                    addRestoredTask(task);
                }
            }

            // Восстанавливаем историю, если она есть
            if (parts.length > 1 && !parts[1].isEmpty()) {
                List<Integer> historyIds = historyFromString(parts[1]);
                for (int id : historyIds) {
                    Task task = getTaskById(id);
                    if (task == null) {
                        task = getSubtaskById(id);
                    }
                    if (task == null) {
                        task = getEpicById(id);
                    }
                    if (task != null) {
                        historyManager.add(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения из файла", e);
        }
    }


    /**
     * Метод преобразует объект Task в строку CSV.
     *
     * @param task Задача для преобразования.
     * @return Строка CSV, представляющая задачу
     */

    private static String toString(Task task) {
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.join(",", Integer.toString(subtask.getId()), "SUBTASK", subtask.getName(), subtask.getStatus().toString(), subtask.getDescription(), Integer.toString(subtask.getEpicId()));
        } else if (task instanceof Epic) {
            return String.join(",", Integer.toString(task.getId()), "EPIC", task.getName(), task.getStatus().toString(), task.getDescription(), "");
        } else {
            return String.join(",", Integer.toString(task.getId()), "TASK", task.getName(), task.getStatus().toString(), task.getDescription(), "");
        }
    }

    /**
     * Метод создает Task из строки CSV.
     *
     * @param value Строка CSV для разбора
     * @return Восстановленный объект Task
     * @throw IllegalArgumentException при неизвестном типе задачи
     */
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case "TASK":
                Task task = new Task(id, name, description, status);
                return task;
            case "EPIC":
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(id, name, description, status, epicId);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);


        }
    }

    // Переопределяем методы родительского класса и сохраняем в файл
    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
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





package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Класс для управления задачами
public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1; // Счетчик для генерации идентификаторов
    protected final Map<Integer, Task> tasks = new HashMap<>(); // Для хранения задач
    protected final Map<Integer, Subtask> subtasks = new HashMap<>(); // Для хранения подзадач
    protected final Map<Integer, Epic> epics = new HashMap<>(); // Для хранения эпиков
    protected final HistoryManager historyManager = Managers.getDefaultHistory(); // Менеджер истории, получаем через Manager

    // Отсортированный набор задач по приоритету
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder()))
    );

    // Метод для генерации нового уникального идентификатора
    private int generateIds() {
        return nextId++;
    }

    // Геттер для получения списка всех задач
    @Override
    public Collection<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Геттер для получения списка всех подзадач
    @Override
    public Collection<Task> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Геттер для получения списка всех эпиков
    @Override
    public Collection<Task> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    /*
        Геттер для получения истории просмотренных задач.
        Возвращает историю просмотров через менеджер истории.
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


    // Метод для удаления всех задач
    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.keySet().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    //Метод для удаления всех подзадач
    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(prioritizedTasks::remove);
        subtasks.clear();

        // Обновляем эпики после удаления подзадач
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
            updateEpicTimeFields(epic);
        });
    }

    //Метод для очистки списка эпиков
    @Override
    public void deleteAllEpics() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(prioritizedTasks::remove);
        subtasks.clear();

        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    /* Обновляем методы получения задач, чтобы они добавлялись в историю
       Получение задачи по идентификатору
     */
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Получение подзадачи по идентификатору
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Получение эпика по идентификатору
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Создание задачи
    @Override
    public int createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть 'null'");
        }
        // Проверка на пересечение по времени
        if (task.getStartTime() != null && hasTaskOverlaps(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей");
        }
        task.setId(nextId++);
        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }

        return task.getId();
    }

    // Создание подзадачи
    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }
        if (subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Подзадача не может ссылаться на саму себя как на эпик");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с id=" + subtask.getEpicId() + " не существует");
        }

        // Проверка на пересечение по времени
        if (subtask.getStartTime() != null && hasTaskOverlaps(subtask)) {
            throw new ManagerSaveException("Подзадача пересекается по времени с существующей задачей");
        }

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask); // Добавляем подзадачу в хранилище
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик к которому относится подзадача
        epic.getSubtaskIds().add(subtask.getId());

        updateEpicStatus(epic);
        updateEpicTimeFields(epic);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        return subtask.getId();
    }

    // Создание эпика
    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic не может быть 'null'");
        }
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача не найдена или 'null'");
        }

        // Проверка на пересечение по времени (исключая саму задачу)
        if (task.getStartTime() != null) {
            boolean hasOverlap = prioritizedTasks.stream()
                    .filter(t -> t.getId() != task.getId())
                    .anyMatch(existingTask -> isTasksOverlap(task, existingTask));

            if (hasOverlap) {
                throw new ManagerSaveException("Задача пересекается по времени с существующей");
            }
        }

        tasks.put(task.getId(), task); // Замена старой задачи на новую

        // Обновляем в prioritizedTasks
        prioritizedTasks.removeIf(t -> t.getId() == task.getId());
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    // Обновление подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача не найдена или 'null'");
        }

        // Проверка на пересечение по времени (исключая саму подзадачу)
        if (subtask.getStartTime() != null) {
            boolean hasOverlap = prioritizedTasks.stream()
                    .filter(t -> t.getId() != subtask.getId())
                    .anyMatch(existingTask -> isTasksOverlap(subtask, existingTask));

            if (hasOverlap) {
                throw new ManagerSaveException("Подзадача пересекается по времени с существующей задачей");
            }
        }
        Subtask savedSubtask = subtasks.get(subtask.getId());
        int oldEpicId = savedSubtask.getEpicId();
        int newEpicId = subtask.getEpicId();

        // Если изменился эпик, обновляем связи
        if (oldEpicId != newEpicId) {
            if (!epics.containsKey(newEpicId)) {
                throw new IllegalArgumentException("New epic not found");
            }

            Epic oldEpic = epics.get(oldEpicId);
            oldEpic.getSubtaskIds().remove((Integer) subtask.getId());
            updateEpicStatus(oldEpic);
            updateEpicTimeFields(oldEpic);

            Epic newEpic = epics.get(newEpicId);
            newEpic.getSubtaskIds().add(subtask.getId());
            updateEpicStatus(newEpic);
            updateEpicTimeFields(newEpic);
        }

        savedSubtask.setStatus(subtask.getStatus());
        savedSubtask.setStartTime(subtask.getStartTime());
        savedSubtask.setDuration(subtask.getDuration());

        // Обновляем в prioritizedTasks
        prioritizedTasks.removeIf(t -> t.getId() == subtask.getId());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        // Обновляем родительский эпик
        Epic epic = epics.get(subtask.getEpicId());
        updateEpicStatus(epic);
        updateEpicTimeFields(epic);
    }


    // Обновление эпика
    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Epic не найден или 'null'");
        }

        // Получаем существующий эпик из хранилища
        Epic savedEpic = epics.get(epic.getId());

        // Обновляем статус эпика
        updateEpicStatus(savedEpic);
    }

    // Удаление задачи по идентификатору
    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
    }

    // Удаление подзадачи по идентификатору
    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic);
                updateEpicTimeFields(epic);
            }
        }
    }

    // Удаление эпика по идентификатору
    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаляем все подзадачи этого эпика
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
                prioritizedTasks.removeIf(t -> t.getId() == subtaskId);
            });
            historyManager.remove(id);
        }
    }

    // Получение списка подзадач определённого эпика
    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return Collections.unmodifiableSet(prioritizedTasks);
    }

    @Override
    public boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    @Override
    public boolean hasTaskOverlaps(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .anyMatch(existingTask -> isTasksOverlap(newTask, existingTask));
    }


    // Обновление статуса эпика на основе статусов его подзадач
    void updateEpicStatus(Epic epic) {
        List<Subtask> subtasks = getSubtasksByEpicId(epic.getId());

        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    /**
     * Обновляет временные параметры эпика (начало, продолжительность, окончание)
     */
    private void updateEpicTimeFields(Epic epic) {
        epic.updateEpicFields(this);
    }
}



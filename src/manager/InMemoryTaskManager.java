package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.*;

// Класс для управления задачами
public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>(); // Для хранения задач
    protected final Map<Integer, Subtask> subtasks = new HashMap<>(); // Для хранения подзадач
    protected final Map<Integer, Epic> epics = new HashMap<>(); // Для хранения эпиков
    protected int nextId = 1; // Счетчик для генерации идентификаторов
    protected final HistoryManager historyManager = Managers.getDefaultHistory(); // Менеджер истории, получаем через Manager

    // Метод для генерации нового уникального идентификатора
    private int generateIds() {
        return nextId++;
    }

    /*
        Геттер для получения истории просмотренных задач.
        Возвращает историю просмотров через менеджер истории.
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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

    // Метод для удаления всех задач
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    //Метод для удаления всех подзадач
    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        // Очистка списка подзадач у всех эпиков и обновление их статусов
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    //Метод для очистки списка эпиков
    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear(); // Удаление всех подзадач, так как они связаны с эпиками
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
    public void createTask(Task task) {
        //task.setId(generateIds()); // Устанавливаем уникальный идентификатор
        tasks.put(task.getId(), task); // Добавляем задачу в хранилище
    }

    // Создание подзадачи
    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }
        if (subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Подзадача не может ссылаться на саму себя как на эпик");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с id=" + subtask.getEpicId() + " не существует");
        }


        subtasks.put(subtask.getId(), subtask); // Добавляем подзадачу в хранилище
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик к которому относится подзадача
        if (epic != null) {
            epic.addSubtaskId(subtask.getId()); // Добавление подзадачи в эпик
            updateEpicStatus(epic.getId()); // Обновление статуса эпика
        }
    }

    // Создание эпика
    @Override
    public void createEpic(Epic epic) {
        //epic.setId(generateIds()); // Устанавливаем уникальный идентификатор
        epics.put(epic.getId(), epic); // Добавление эпика в хранилище
    }

    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task); // Замена старой задачи на новую
    }

    // Обновление подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask); // Замена старой подзадачи на новую
        updateEpicStatus(subtask.getEpicId()); // Обновление статуса эпика
    }

    // Обновление эпика
    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic); // Замена старого эпика на новый
        updateEpicStatus(epic.getId()); // Обновление статуса эпика
    }

    // Удаление задачи по идентификатору
    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
        }
    }

    // Удаление подзадачи по идентификатору
    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            // Удаляем подзадачу из эпика
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    // Удаление эпика по идентификатору
    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаляем все подзадачи этого эпика
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    // Получение списка подзадач определённого эпика
    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId); // Получение эпика по идентификатору
        if (epic != null) {
            // Добавление всех подзадач эпика в результат
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        }
        return result;
    }

    // Обновление статуса эпика на основе статусов его подзадач
    void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId); // Получение эпика по идентификатору
        if (epic != null) {
            List<Integer> subtaskIds = epic.getSubtaskIds(); // Получение списка подзадач
            if (subtaskIds.isEmpty()) {
                // Если подзадач нет, статус эпика — NEW
                epic.setStatus(Status.NEW);
                return;
            }

            boolean allDone = true;  // Все подзадачи завершены
            boolean allNew = true;   // Все подзадачи новые

            // Проверка статусов всех подзадач
            for (int subtaskId : subtaskIds) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    if (subtask.getStatus() != Status.DONE) {
                        allDone = false;
                    }
                    if (subtask.getStatus() != Status.NEW) {
                        allNew = false;
                    }
                }
            }

            // Установка статуса эпика
            if (allDone) {
                epic.setStatus(Status.DONE);
            } else if (allNew) {
                epic.setStatus(Status.NEW);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }
}



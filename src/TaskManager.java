import java.util.ArrayList;
import java.util.HashMap;
import  java.util.List;

// Класс для управления задачами
public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>(); // Для хранения задач
    private HashMap<Integer, Subtask> subtasks = new HashMap<>(); // Для хранения подзадач
    private HashMap<Integer, Epic> epics = new HashMap<>(); // Для хранения эпиков
    private int nextId = 1; // Счетчик для генерации идентификаторов

    // Метод для генерации нового уникального идентификатора
    private int generateIds() {
        return nextId++;
    }
    // Геттер для получения списка всех задач
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
    // Геттер для получения списка всех подзадач
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }
    // Геттер для получения списка всех эпиков
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Метод для удаления всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    //Метод для удаления всех подзадач
    public void deleteAllSubtasks() {
        subtasks.clear();
        // Очистка списка подзадач у всех эпиков и обновление их статусов
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    //Метод для очистки списка эпиков
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear(); // Удаление всех подзадач, так как они связаны с эпиками
    }

    // Получение задачи по идентификатору
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    // Получение подзадачи по идентификатору
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    // Получение эпика по идентификатору
    public Epic getEpicById(int id) {
        return  epics.get(id);
    }

    // Создание задачи
    public void createTask(Task task) {
        task.setId(generateIds()); // Устанавливаем уникальный идентификатор
        tasks.put(task.getId(), task); // Добавляем задачу в хранилище
    }

    // Создание подзадачи
    public void createSubtask(Subtask subtask) {
        subtask.setId(generateIds()); // Устанавливаем уникальный идентификатор
        subtasks.put(subtask.getId(), subtask); // Добавляем подзадачу в хранилище
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик к которому относится подзадача
        if (epic != null) {
            epic.addSubtaskId(subtask.getId()); // Добавление подзадачи в эпик
            updateEpicStatus(epic.getId()); // Обновление статуса эпика
        }
    }

    // Создание эпика
    public void createEpic(Epic epic) {
        epic.setId(generateIds()); // Устанавливаем уникальный идентификатор
        epics.put(epic.getId(), epic); // Добавление эпика в хранилище
    }

    // Обновление задачи
    public void updateTask(Task task) {
        tasks.put(task.getId(), task); // Замена старой задачи на новую
    }

    // Обновление подзадачи
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask); // Замена старой подзадачи на новую
        updateEpicStatus(subtask.getEpicId()); // Обновление статуса эпика
    }

    // Обновление эпика
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic); // Замена старого эпика на новый
        updateEpicStatus(epic.getId()); // Обновление статуса эпика
    }

    // Удаление задачи по идентификатору
    public void deleteTaskById(int id) {
        tasks.remove(id); // Удаление задачи из хранилища
    }

    // Удаление подзадачи по идентификатору
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id); // Удаление подзадачи из хранилища
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId()); // Получение эпика, к которому относится подзадача
            if (epic != null) {
                epic.removeSubtaskId(id); // Удаление подзадачи из эпика
                updateEpicStatus(epic.getId()); // Обновление статуса эпика
            }
        }
    }

    // Удаление эпика по идентификатору
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id); // Удаление эпика из хранилища
        if (epic != null) {
            // Удаление всех подзадач, связанных с этим эпиком
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    // Получение списка подзадач определённого эпика
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
    private void updateEpicStatus(int epicId) {
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



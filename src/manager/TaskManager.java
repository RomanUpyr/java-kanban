package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface TaskManager {
    // Геттер для получения списка всех задач
    Collection<Task> getAllTasks();

    // Геттер для получения списка всех подзадач
    Collection<Task> getAllSubtasks();

    // Геттер для получения списка всех эпиков
    Collection<Task> getAllEpics();

    // Метод для удаления всех задач
    void deleteAllTasks();

    //Метод для удаления всех подзадач
    void deleteAllSubtasks();

    //Метод для очистки списка эпиков
    void deleteAllEpics();

    // Получение задачи по идентификатору
    Task getTaskById(int id);

    // Получение подзадачи по идентификатору
    Subtask getSubtaskById(int id);

    // Получение эпика по идентификатору
    Epic getEpicById(int id);

    // Создание задачи
    void createTask(Task task);

    // Создание подзадачи
    void createSubtask(Subtask subtask);

    // Создание эпика
    void createEpic(Epic epic);

    // Обновление задачи
    void updateTask(Task task);

    // Обновление подзадачи
    void updateSubtask(Subtask subtask);

    // Обновление эпика
    void updateEpic(Epic epic);

    // Удаление задачи по идентификатору
    void deleteTaskById(int id);

    // Удаление подзадачи по идентификатору
    void deleteSubtaskById(int id);

    // Удаление эпика по идентификатору
    void deleteEpicById(int id);

    // Получение списка подзадач определённого эпика
    List<Subtask> getSubtasksByEpicId(int epicId);

    // Возвращает список последних 10 просмотренных задач, в порядке из просмотра (от старых к новым)
    List <Task> getHistory();
}

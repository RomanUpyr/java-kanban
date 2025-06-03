import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        // Создание двух задач
        Task task1 = new Task(0, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(0, "Task 2", "Description 2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпика с тремя подзадачами
        Epic epic1 = new Epic(0, "Epic 1", "Description Epic 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description Subtask 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description Subtask 2", Status.IN_PROGRESS, epic1.getId());
        Subtask subtask3 = new Subtask(0, "Subtask 3", "Description Subtask 3", Status.DONE, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);

        // Создание эпика без подзадач
        Epic epic2 = new Epic(0, "Epic 2", "Description Epic 2");
        manager.createEpic(epic2);

        // Запрос задач в разном порядке несколько раз
        System.out.println("Запрашиваем задачи в разном порядке...");
        System.out.println("Запрос задачи 1");
        manager.getTaskById(task1.getId());
        printHistory(manager);

        System.out.println("Запрос эпика 1");
        manager.getEpicById(epic1.getId());
        printHistory(manager);

        System.out.println("Запрос подзадачи 2");
        manager.getSubtaskById(subtask2.getId());
        printHistory(manager);

        System.out.println("Запрос задачи 1 еще раз");
        manager.getTaskById(task1.getId());
        printHistory(manager); // В истории не должно быть дубликатов

        System.out.println("Запрос подзадачи 3");
        manager.getSubtaskById(subtask3.getId());
        printHistory(manager);

        System.out.println("Запрос эпика 2");
        manager.getEpicById(epic2.getId());
        printHistory(manager);

        // Удаление задачи, которая есть в истории
        System.out.println("Удаляем задачу 1...");
        manager.deleteTaskById(task1.getId());
        printHistory(manager); // Задача 1 не должна быть в истории

        // Удаление эпика с тремя подзадачами
        System.out.println("Удаляем эпик 1 с подзадачами...");
        manager.deleteEpicById(epic1.getId());
        printHistory(manager); // Эпик 1 и все его подзадачи не должны быть в истории

        // Финальное состояние истории
        System.out.println("Финальное состояние истории:");
        printHistory(manager);
    }

    private static void printHistory(InMemoryTaskManager manager) {
        System.out.println("Текущая история просмотров:");
        System.out.println(manager.getHistory());
        System.out.println();
    }
}
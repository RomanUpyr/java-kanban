import manager.*;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Тестирование InMemoryTaskManager
        testInMemoryTaskManager();

        // Тестирование FileBackedTaskManager
        testFileBackedTaskManager();
    }

    private static void testInMemoryTaskManager() {
        System.out.println("*** Тестирование InMemoryTaskManager ***");
        InMemoryTaskManager manager = new InMemoryTaskManager();

        // Создаем тестовые данные
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "Description Epic 1");
        manager.createEpic(epic1);


        Subtask subtask1 = new Subtask("Subtask 1", "Description Subtask 1",
                Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description Subtask 2",
                Status.IN_PROGRESS, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Тестируем историю просмотров
        System.out.println("\nТестирование истории просмотров:");
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());
        printHistory(manager);

        // Тестируем удаление
        System.out.println("\nПосле удаления задачи " + task1.getId() + ":");
        manager.deleteTaskById(task1.getId());
        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            // Чёткое разделение типов задач при выводе
            if (task instanceof Epic) {
                System.out.println("[Epic] " + task);
            } else if (task instanceof Subtask) {
                System.out.println("[Subtask] " + task);
            } else {
                System.out.println("[Task] " + task);
            }
        }
    }

    private static void testFileBackedTaskManager() {
        System.out.println("\n*** Тестирование FileBackedTaskManager ***");

        try {
            // Создаем временный файл
            File file = File.createTempFile("tasks", ".csv");
            System.out.println("Используем временный файл: " + file.getAbsolutePath());

            // Создаем и заполняем первый менеджер
            FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

            Task task = new Task(1, "Task", "Description", Status.NEW);
            manager1.createTask(task);

            Epic epic = new Epic(1, "Epic", "Epic Description");
            manager1.createEpic(epic);

            Subtask subtask = new Subtask(2, "Subtask", "Subtask Description",
                    Status.DONE, epic.getId());
            manager1.createSubtask(subtask);

            System.out.println("\nДанные до сохранения:");
            printAllTasks(manager1);

            // Создаем второй менеджер из файла
            FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

            System.out.println("\nДанные после загрузки:");
            printAllTasks(manager2);

            // Сравниваем данные
            System.out.println("\nРезультаты сравнения:");
            compareManagers(manager1, manager2);

        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }

    private static void printHistory(InMemoryTaskManager manager) {
        List<Task> history = manager.getHistory();
        if (history.isEmpty()) {
            System.out.println("История просмотров пуста");
        } else {
            System.out.println("История просмотров:");
            history.forEach(task -> System.out.println("  " + task));
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Обычные задачи:");
        manager.getAllTasks().forEach(t -> System.out.println("  " + t));

        System.out.println("\nЭпики:");
        manager.getAllEpics().forEach(e -> {
            System.out.println("  " + e);
            System.out.println("    Подзадачи:");
            manager.getSubtasksByEpicId(e.getId()).forEach(s ->
                    System.out.println("      " + s));
        });

        System.out.println("\nВсе подзадачи:");
        manager.getAllSubtasks().forEach(s -> System.out.println("  " + s));
    }

    private static void compareManagers(TaskManager m1, TaskManager m2) {
        boolean tasksEqual = m1.getAllTasks().equals(m2.getAllTasks());
        boolean epicsEqual = m1.getAllEpics().equals(m2.getAllEpics());
        boolean subtasksEqual = m1.getAllSubtasks().equals(m2.getAllSubtasks());

        System.out.println("Обычные задачи: " + (tasksEqual ? "совпадают" : "различаются"));
        System.out.println("Эпики: " + (epicsEqual ? "совпадают" : "различаются"));
        System.out.println("Подзадачи: " + (subtasksEqual ? "совпадают" : "различаются"));

        if (tasksEqual && epicsEqual && subtasksEqual) {
            System.out.println("Все данные успешно сохранены и загружены!");
        }
    }
}
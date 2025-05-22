import manager.*;
import manager.TaskManager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Тестирование InMemoryTaskManager
        testInMemoryTaskManager();

        // Тестирование FileBackedTaskManager
        testFileBackedTaskManager();
    }

    private static void testInMemoryTaskManager() {
        System.out.println("*** Тестирование InMemoryTaskManager ***");
        TaskManager manager = Managers.getDefault();

        // Создаем тестовые данные
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "Description Epic 1");
        int epicId = manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description Subtask 1",
                Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description Subtask 2",
                Status.IN_PROGRESS, epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Тестируем историю просмотров
        System.out.println("\nТестирование истории просмотров:");
        manager.getTaskById(task1.getId());
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtask1.getId());
        printHistory(manager);

        // Тестируем удаление
        System.out.println("\nПосле удаления задачи " + task1.getId() + ":");
        manager.deleteTaskById(task1.getId());
        printHistory(manager);
    }

    private static void testFileBackedTaskManager() {
        System.out.println("\n*** Тестирование FileBackedTaskManager ***");

        try {
            // Создаем временный файл
            File file = File.createTempFile("tasks", ".csv");
            System.out.println("Используем временный файл: " + file.getAbsolutePath());

            // Создаем и заполняем менеджер
            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            // 1. Сначала создаем эпик и получаем его ID
            Epic epic = new Epic("Epic", "Description");
            int epicId = manager.createEpic(epic);
            System.out.println("Создан эпик с ID: " + epicId);

            // 2. Проверяем, что ID эпика валиден
            if (epicId <= 0) {
                throw new IllegalStateException("Неверный ID эпика: " + epicId);
            }

            // 3. Создаем подзадачу с корректным ID эпика
            Subtask subtask = new Subtask(
                    "Subtask",
                    "Description",
                    Status.NEW,
                    epicId  // Используем полученный ID эпика
            );
            int subtaskId = manager.createSubtask(subtask);
            System.out.println("Создана подзадача с ID: " + subtaskId);

            // 4. Проверяем сохранение/загрузку
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
            System.out.println("\nЗагруженные данные:");
            printAllTasks(loadedManager);

        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Ошибка создания задач: " + e.getMessage());
        }
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
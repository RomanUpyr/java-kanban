import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

// Тестовый класс для задачи
public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание задач
        Task task1 = new Task(0, "model.Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(0, "model.Task 2", "Description 2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпика с подзадачами
        Epic epic1 = new Epic(0, "model.Epic 1", "Description model.Epic 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "model.Subtask 1", "Description model.Subtask 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "model.Subtask 2", "Description model.Subtask 2", Status.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Вывод всех задач
        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());
        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());

        // Изменение статусов
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        // Вывод обновлённых данных
        System.out.println("После изменения статуса подзадачи 1:");
        System.out.println(manager.getAllSubtasks());
        System.out.println("Статус эпика 1:");
        System.out.println(manager.getEpicById(epic1.getId()).getStatus());

        // Удаление задачи и эпика
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());

        // Вывод после удаления
        System.out.println("После удаления задачи 1 и эпика 1:");
        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());
        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());
    }

}

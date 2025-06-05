package handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.util.Map;

/**
 * Обработчик HTTP-запросов для подзадач (Subtask):
 * - GET /subtasks — все подзадачи
 * - GET /subtasks/{id} — подзадача по ID
 * - GET /subtasks/epic/{epicId} — подзадачи эпика
 * - POST /subtasks — создать/обновить подзадачу
 * - DELETE /subtasks — удалить все подзадачи
 * - DELETE /subtasks/{id} — удалить подзадачу
 */
public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            System.out.println("Request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            switch (method) {
                case "GET":
                    if (pathParts.length == 2) { // GET /subtasks
                        sendSuccess(exchange, GSON.toJson(taskManager.getAllSubtasks()));
                    } else if (pathParts.length == 3) { // GET /subtasks/{id}
                        int id = Integer.parseInt(pathParts[2]);
                        Subtask subtask = taskManager.getSubtaskById(id);
                        if (subtask != null) {
                            sendSuccess(exchange, GSON.toJson(subtask));
                        } else {
                            sendNotFound(exchange);
                        }
                    } else if (pathParts.length == 4 && pathParts[2].equals("epic")) { // GET /subtasks/epic/{epicId}
                        int epicId = Integer.parseInt(pathParts[3]);
                        sendSuccess(exchange, GSON.toJson(taskManager.getSubtasksByEpicId(epicId)));
                    }
                    break;
                case "POST":
                    Subtask newSubtask = parseJson(exchange.getRequestBody(), Subtask.class);
                    System.out.println("Parsed subtask: " + newSubtask);
                    if (taskManager.hasTaskOverlaps(newSubtask)) {
                        System.out.println("Time overlap detected");
                        sendNotAcceptable(exchange);
                    } else {
                        int subtaskId = taskManager.createSubtask(newSubtask);
                        System.out.println("Created subtask with id: " + subtaskId);
                        sendCreated(exchange, GSON.toJson(Map.of("id", subtaskId)));
                    }
                    break;
                case "DELETE":
                    if (pathParts.length == 2) { // DELETE /subtasks
                        taskManager.deleteAllSubtasks();
                        sendCreated(exchange, "All subtasks deleted");
                    } else if (pathParts.length == 3) { // DELETE /subtasks/{id}
                        int id = Integer.parseInt(pathParts[2]);
                        taskManager.deleteSubtaskById(id);
                        sendCreated(exchange, "Subtask deleted");
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            System.err.println("Error in SubtasksHandler:");
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }
}

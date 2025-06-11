package handler;

import com.sun.net.httpserver.HttpExchange;
import manager.ManagerSaveException;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Обработчик HTTP-запросов для обычных задач (Task):
 * - GET /tasks — получить все задачи
 * - GET /tasks/{id} — получить задачу по ID
 * - POST /tasks — создать/обновить задачу
 * - DELETE /tasks — удалить все задачи
 * - DELETE /tasks/{id} — удалить задачу по ID
 */
public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (ManagerSaveException e) {
            if (e.getMessage().contains("Задача пересекается по времени с существующей") || e.getMessage().contains("overlap")) {
                sendResponse(exchange, 406, e.getMessage());
            } else {
                sendResponse(exchange, 500, "Internal server error");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length == 2) { // GET /tasks
            Collection<Task> tasks = taskManager.getAllTasks();
            sendSuccess(exchange, GSON.toJson(tasks));
        } else if (pathParts.length == 3) { // GET /tasks/{id}
            try {
                int id = Integer.parseInt(pathParts[2]);
                Task task = taskManager.getTaskById(id);
                if (task != null) {
                    sendSuccess(exchange, GSON.toJson(task));
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Task task = GSON.fromJson(body, Task.class);
        task.setId(0); // Сбрасываем ID для новой задачи

        int taskId = taskManager.createTask(task);
        sendText(exchange, GSON.toJson(Map.of("id", taskId)), 201);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendText(exchange, "All tasks deleted", 200);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}

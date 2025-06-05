package handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Обработчик HTTP-запросов для эпиков (Epic):
 * - GET /epics — все эпики
 * - GET /epics/{id} — эпик по ID
 * - POST /epics — создать/обновить эпик
 * - DELETE /epics — удалить все эпики
 * - DELETE /epics/{id} — удалить эпик
 */
public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            switch (method) {
                case "GET":
                    handleGetRequest(exchange, pathParts);
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, pathParts);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Invalid ID format", 400); // Bad Request
        } catch (NoSuchElementException e) {
            sendNotFound(exchange);
        } catch (IllegalArgumentException e) {
            sendText(exchange, e.getMessage(), 406); // Not Acceptable
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2) { // GET /epics
            sendSuccess(exchange, GSON.toJson(taskManager.getAllEpics()));
        } else if (pathParts.length == 3) { // GET /epics/{id}
            int id = Integer.parseInt(pathParts[2]);
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                sendSuccess(exchange, GSON.toJson(epic));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        Epic epic = parseJson(exchange.getRequestBody(), Epic.class);

        if (epic == null) {
            sendText(exchange, "Epic не может быть равен нулю", 400);
            return;
        }

        // Валидация данных эпика
        if (epic.getName() == null || epic.getName().isBlank()) {
            sendText(exchange, "Epic не может быть пустым", 400);
            return;
        }

        // Проверка на конфликты времени для подзадач
        if (taskManager.hasTaskOverlaps(epic)) {
            sendNotAcceptable(exchange);
            return;
        }

        int epicId;
        if (epic.getId() == 0) { // Новая задача
            epicId = taskManager.createEpic(epic);
            sendCreated(exchange, GSON.toJson(Map.of("id", epicId)));
        } else { // Обновление существующей
            taskManager.updateEpic(epic);
            sendSuccess(exchange, GSON.toJson(Map.of("id", epic.getId())));
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2) { // DELETE /epics
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "Все эпики удалены");
        } else if (pathParts.length == 3) { // DELETE /epics/{id}
            int id = Integer.parseInt(pathParts[2]);
            if (taskManager.getEpicById(id) != null) {
                taskManager.deleteEpicById(id);
                sendSuccess(exchange, "Epic удален");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

}

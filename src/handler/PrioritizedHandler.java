package handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;

/**
 * Обработчик для получения задач в порядке приоритета.
 * Поддерживает только GET /prioritized.
 */
public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendSuccess(exchange, GSON.toJson(taskManager.getPrioritizedTasks()));
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}

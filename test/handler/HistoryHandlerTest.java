package handler;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerTest extends HttpTaskServerTest {
    @BeforeEach
    void resetCounter() {
        Task.resetCounter();
    }

    private Task createTestTask(String name) {
        return new Task(name, "Description", Status.NEW);
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        // Подготовка: создаём задачу и запрашиваем её (чтобы добавить в историю)
        Task task = createTestTask("For history");
        int taskId = taskManager.createTask(task);
        taskManager.getTaskById(taskId); // Добавляем в историю

        // Выполнение запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверки
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("For history"));
        assertTrue(response.body().contains("\"status\":\"NEW\""));
    }

    @Test
    void testEmptyHistory() throws IOException, InterruptedException {
        // Выполнение запроса без истории
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверки
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    void testHistoryWithMultipleTasks() throws IOException, InterruptedException {
        // Подготовка
        Task task1 = createTestTask("Task 1");
        Task task2 = createTestTask("Task 2");

        int taskId1 = taskManager.createTask(task1);
        int taskId2 = taskManager.createTask(task2);

        // Добавляем в историю в определенном порядке
        taskManager.getTaskById(taskId2);
        taskManager.getTaskById(taskId1);

        // Выполнение запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверки
        assertEquals(200, response.statusCode());

        // Проверяем порядок задач в истории (последняя запрошенная должна быть первой)
        int indexTask1 = response.body().indexOf("Task 1");
        int indexTask2 = response.body().indexOf("Task 2");
        assertTrue(indexTask2 < indexTask1, "Задачи должны быть в порядке добавления в историю");
    }
}
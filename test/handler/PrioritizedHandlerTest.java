package handler;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest extends HttpTaskServerTest {
    @BeforeEach
    void resetCounter() {
        Task.resetCounter();
    }

    private Task createTaskWithTime(String name, Duration duration, LocalDateTime startTime) {
        Task task = new Task(name, "Description", Status.NEW);
        task.setDuration(duration);
        task.setStartTime(startTime);
        return task;
    }

    @Test
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Подготовка данных
        LocalDateTime now = LocalDateTime.now();
        Task task1 = createTaskWithTime("Task 1", Duration.ofMinutes(30), now.plusHours(1));
        Task task2 = createTaskWithTime("Task 2", Duration.ofMinutes(45), now);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Выполнение запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверки
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
        assertTrue(response.body().contains("\"duration\":1800")); // 30 минут в секундах
        assertTrue(response.body().contains("\"duration\":2700")); // 45 минут в секундах

    }

    @Test
    void testGetPrioritizedTasksWithoutTime() throws IOException, InterruptedException {
        // Подготовка данных
        Task taskWithoutTime = new Task("No time task", "Description", Status.NEW);
        taskManager.createTask(taskWithoutTime);

        // Выполнение запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверка
        assertEquals(200, response.statusCode());
    }

    @Test
    void testEmptyPrioritizedTasks() throws IOException, InterruptedException {
        // Выполнение запроса без задач
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверки
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }
}
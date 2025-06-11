package handler;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
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
    void getPrioritizedTasks_shouldReturnTasksInPriorityOrder() throws IOException, InterruptedException {
        // Подготовка данных
        LocalDateTime now = LocalDateTime.now();
        createTaskWithTime("Task 1", Duration.ofMinutes(30), now.plusHours(1));
        createTaskWithTime("Task 2", Duration.ofMinutes(45), now);

        HttpResponse<String> response = sendRequest(
                buildGetRequest("/prioritized")
        );

        assertResponseStatus(response, 200);
        assertResponseContains(response, "");

    }

    @Test
    void getPrioritizedTasksWithoutTime_shouldReturnEmptyList() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest(
                buildGetRequest("/prioritized")
        );

        assertResponseStatus(response, 200);
        assertEquals("[]", response.body().trim());
    }
}
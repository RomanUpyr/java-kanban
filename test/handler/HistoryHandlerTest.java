package handler;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerTest extends HttpTaskServerTest {
    @BeforeEach
    void resetCounter() {
        Task.resetCounter();
    }

    private Task createTestTask(String name) {
        Task task = new Task(name, "Description", Status.NEW);
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId()); // Добавляем в историю
        return task;
    }

    @Test
    void getHistory_shouldReturnTasksInCorrectOrder() throws IOException, InterruptedException {
        Task task1 = createTestTask("Task 1");
        Task task2 = createTestTask("Task 2");

        HttpResponse<String> response = sendRequest(
                buildGetRequest("/history")
        );

        assertResponseStatus(response, 200);
        assertResponseContains(response, "Task 1");
        assertResponseContains(response, "Task 2");
    }

    @Test
    void getEmptyHistory_shouldReturnEmptyArray() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest(
                buildGetRequest("/history")
        );

        assertResponseStatus(response, 200);
        assertEquals("[]", response.body().trim());
    }
}
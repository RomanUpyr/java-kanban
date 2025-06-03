package handler;

import model.*;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtasksHandlerTest extends HttpTaskServerTest {
    private Epic createTestEpic() {
        return new Epic("Test Epic", "Description");
    }

    private Subtask createTestSubtask(int epicId) {
        return new Subtask("Test Subtask", "Description", Status.NEW, epicId);
    }

    private Subtask createTestSubtaskWithTime(int epicId) {
        Subtask subtask = new Subtask("Timed Subtask", "Description", Status.NEW, epicId);
        subtask.setDuration(Duration.ofMinutes(30));
        subtask.setStartTime(LocalDateTime.now().plusHours(1));
        return subtask;
    }

    @Test
    void testGetSubtasksByEpic() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = createTestSubtask(epicId);
        taskManager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/epic/" + epicId))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Subtask"));
        assertTrue(response.body().contains("\"epicId\":" + epicId));
    }

    @Test
    void testCreateSubtaskWithTime() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = createTestSubtaskWithTime(epicId);
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = sendRequest(request);

        assertEquals(201, response.statusCode());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testSubtaskTimeOverlap() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        int epicId = taskManager.createEpic(epic);

        Subtask existing = createTestSubtaskWithTime(epicId);
        taskManager.createSubtask(existing);
        System.out.println("Existing subtask: " + existing);

        Subtask overlapping = createTestSubtaskWithTime(epicId);
        overlapping.setStartTime(existing.getStartTime().plusMinutes(15));
        String json = gson.toJson(overlapping);
        System.out.println("Sending JSON: " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = sendRequest(request);
        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        assertEquals(406, response.statusCode(),
                "Expected 406 for time overlap. Response: " + response.body());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = createTestSubtask(epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();
        HttpResponse<String> response = sendRequest(request);

        assertEquals(201, response.statusCode());
        assertNull(taskManager.getSubtaskById(subtaskId));
        assertEquals(0, taskManager.getSubtasksByEpicId(epicId).size());
    }
}
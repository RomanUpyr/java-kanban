package handler;

import model.*;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class EpicsHandlerTest extends HttpTaskServerTest {
    @Test
    void createTask_shouldReturnCreatedStatus() throws IOException, InterruptedException {
        // Подготовка данных
        Task task = new Task("New Task", "Description", Status.NEW);
        HttpResponse<String> response = sendRequest(
                buildPostRequest("/tasks", task)
        );

        assertResponseStatus(response, 201);
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    void getNonExistentEpic_shouldReturnNotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest(
                buildGetRequest("/epics/999")
        );

        assertResponseStatus(response, 404);
    }
}
package handler;

import model.*;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class EpicsHandlerTest extends HttpTaskServerTest {
    @Test
    void testCreateTask() throws IOException, InterruptedException {
        // Подготовка данных
        Task task = new Task("New Task", "Description", Status.NEW);
        String json = gson.toJson(task);

        // Выполнение запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = sendRequest(request);

        // Добавим вывод для диагностики
        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        assertEquals(201, response.statusCode(),
                "Ожидался статус 201 Created. Тело ответа: " + response.body());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    void testGetEpicNotFound() throws IOException, InterruptedException {
        // Выполнение запроса к несуществующему эпику
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();
        HttpResponse<String> response = sendRequest(request);

        // Проверки
        assertEquals(404, response.statusCode());
    }
}
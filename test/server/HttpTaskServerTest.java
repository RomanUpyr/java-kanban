package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import handler.DurationAdapter;
import handler.LocalDateTimeAdapter;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public abstract class HttpTaskServerTest {
    protected HttpTaskServer server;
    protected TaskManager taskManager;
    protected HttpClient client;
    protected Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = new HttpTaskServer(taskManager);
        server.start();
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    protected HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path));
    }

    protected HttpRequest buildGetRequest(String path) {
        return requestBuilder(path).GET().build();
    }

    protected HttpRequest buildPostRequest(String path, Object body) {
        return requestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .header("Content-Type", "application/json")
                .build();
    }

    protected HttpRequest buildDeleteRequest(String path) {
        return requestBuilder(path).DELETE().build();
    }

    protected void assertResponseStatus(HttpResponse<String> response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(),
                "Неожиданный статус. Тело ответа: " + response.body());
    }

    protected void assertResponseContains(HttpResponse<String> response, String expectedContent) {
        assertTrue(response.body().contains(expectedContent),
                "Тело ответа должно содержать: " + expectedContent + "\nActual: " + response.body());
    }
}
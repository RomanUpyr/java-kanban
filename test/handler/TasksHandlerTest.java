package handler;

import manager.*;
import org.junit.jupiter.api.*;
import server.HttpTaskServer;

import java.io.IOException;

class TasksHandlerTest {
    private static HttpTaskServer server;

    @BeforeAll
    static void startServer() throws IOException {
        TaskManager taskManager = Managers.getDefault();
        server = new HttpTaskServer(taskManager);
        server.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }
}
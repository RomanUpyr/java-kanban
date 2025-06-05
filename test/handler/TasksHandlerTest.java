package handler;

import server.HttpTaskServerTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TasksHandlerTest extends HttpTaskServerTest {
    @Test
    void server_shouldStartAndStopCorrectly() {
        assertNotNull(server);
        assertNotNull(taskManager);
    }
}
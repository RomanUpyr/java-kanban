package handler;

import model.*;
import org.junit.jupiter.api.Test;
import server.HttpTaskServerTest;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtasksHandlerTest extends HttpTaskServerTest {
    private Epic createTestEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.createEpic(epic);
        return epic;
    }

    private Subtask createTestSubtask(int epicId) {
        return new Subtask("Test Subtask", "Description", Status.NEW, epicId);
    }

    private Subtask createTestSubtaskWithTime(int epicId) {
        Subtask subtask = new Subtask("Timed Subtask", "Description", Status.NEW, epicId);
        taskManager.createSubtask(subtask);
        return subtask;
    }

    @Test
    void getSubtasksByEpic_shouldReturnSubtasks() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        createTestSubtask(epic.getId());

        HttpResponse<String> response = sendRequest(
                buildGetRequest("/subtasks/epic/" + epic.getId())
        );

        assertResponseStatus(response, 200);
        assertResponseContains(response, "");
    }

    @Test
    void createSubtaskWithOverlappingTime_shouldReturnNotAcceptable() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        Subtask existing = new Subtask("Existing", "Description", Status.NEW, epic.getId());
        existing.setDuration(Duration.ofMinutes(30));
        existing.setStartTime(LocalDateTime.now().plusHours(1));
        taskManager.createSubtask(existing);

        Subtask overlapping = new Subtask("Overlapping", "Description", Status.NEW, epic.getId());
        overlapping.setDuration(Duration.ofMinutes(30));
        overlapping.setStartTime(existing.getStartTime().plusMinutes(15));

        HttpResponse<String> response = sendRequest(
                buildPostRequest("/subtasks", overlapping)
        );

        assertResponseStatus(response, 406);
    }


}
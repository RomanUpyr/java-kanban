package manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultShouldReturnInitializedInMemoryTaskManager() {
        // Проверяем, что getDefault() возвращает проинициализированный InMemoryTaskManager
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер не должен быть null");
        assertInstanceOf(InMemoryTaskManager.class, manager, "Должен возвращаться InMemoryTaskManager");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedInMemoryHistoryManager() {
        // Проверяем, что getDefaultHistory() возвращает проинициализированный InMemoryHistoryManager
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");
        assertInstanceOf(InMemoryHistoryManager.class, historyManager, "Должен возвращаться InMemoryHistoryManager");
    }
}
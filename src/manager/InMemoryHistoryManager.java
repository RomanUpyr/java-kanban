package manager;

import model.Task;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager{
    private final LinkedList<Task> history = new LinkedList<>(); // Для хранения истории просмотров
    private static final int MAX_HISTORY_SIZE = 10; // Максимальный размер истории просмотров

    /*
        Метод добавляет задачу в конец истории.
        Если размер истории превышает MAX_HISTORY_SIZE, удаляет самый старый элемент
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        history.addLast(task);

        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }

    }
    // Метод возвращает копию текущей истории просмотров
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}

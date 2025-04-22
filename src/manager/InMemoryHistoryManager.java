package manager;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    /**
     * Добавляет задачу в конец двусвязного списка
     */
    private void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(task, oldTail, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
    }

    /**
     * Удаляет узел из двусвязного списка
     */
    private void removeNode(Node node) {
        // Обновляем ссылки соседних узлов
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next; // Удаляемый узел был головой
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev; // Удаляемый узел был хвостом
        }
    }

    /**
     * Собирает все задачи из двусвязного списка в ArrayList
     */
    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        // Удаляем задачу, если она уже есть в истории
        remove(task.getId());

        // Добавляем задачу в конец списка
        linkLast(task);
        // Сохраняем ссылку на узел в мапу
        nodeMap.put(task.getId(), tail);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
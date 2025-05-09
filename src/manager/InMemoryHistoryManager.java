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
            oldTail.setNext(newNode);
        }
    }

    /**
     * Удаляет узел из двусвязного списка
     */
    private void removeNode(Node node) {
        // Обновляем связи соседних узлов
        if (node.getPrev() != null) {
            node.getPrev().setNext(node.getNext());
        } else {
            head = node.getNext(); // Удаляемый узел был головой
        }

        if (node.getNext() != null) {
            node.getNext().setPrev(node.getPrev());
        } else {
            tail = node.getPrev(); // Удаляемый узел был хвостом
        }

        // Удаляем узел из мапы
        nodeMap.remove(node.getTask().getId());
    }

    /**
     * Собирает все задачи из двусвязного списка в ArrayList
     */
    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.getTask());
            current = current.getNext();
        }
        return tasks;
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Удаляем задачу, если она уже есть в истории
        remove(task.getId());

        // Добавляем задачу в конец списка
        linkLast(task);
        // Сохраняем ссылку на узел в мапу
        nodeMap.put(task.getId(), tail);
    }

    @Override
    public void remove(int id) {
        if (nodeMap.containsKey(id)) {  // Проверяем наличие ключа
            removeNode(nodeMap.get(id)); // Удаляем узел если он есть
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
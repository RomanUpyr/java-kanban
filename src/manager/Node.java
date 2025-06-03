package manager;

import model.Task;

/**
 * Узел двусвязного списка, хранящий задачу (Task) и ссылки на предыдущий и следующий узлы.
 */
public class Node {
    private Task task;      // Сама задача, которая хранится в узле
    private Node prev;      // Ссылка на предыдущий узел в списке
    private Node next;      // Ссылка на следующий узел в списке

    /**
     * Конструктор узла.
     */
    public Node(Task task, Node prev, Node next) {
        if (task == null) {
            throw new IllegalArgumentException("Task не может быть равным нулю");
        }
        this.task = task;
        this.prev = prev;
        this.next = next;
    }

    public Task getTask() {
        return new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus());
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }


}
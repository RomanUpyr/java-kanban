package manager;

import model.Task;

/**
 Узел двусвязного списка, хранящий задачу (Task) и ссылки на предыдущий и следующий узлы.
  */
public class Node {
    Task task;      // Сама задача, которая хранится в узле
    Node prev;      // Ссылка на предыдущий узел в списке
    Node next;      // Ссылка на следующий узел в списке

    /**
     * Конструктор узла.
     */
    public Node(Task task, Node prev, Node next) {
        this.task = task;
        this.prev = prev;
        this.next = next;
    }
}
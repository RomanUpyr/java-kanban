package manager;

import model.Task;
import java.util.List;

public interface HistoryManager {

    /* Метод добавляет задачи в историю просмотров.
       Помечает задачи как просмотренные.
    */

    void add(Task task);

    /* Метод возвращает список задач.
       Список последних просмотренных задач в порядке их просмотра.
     */
    List<Task> getHistory();
}


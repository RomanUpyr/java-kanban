package manager;

import model.Task;
import java.util.List;

public interface HistoryManager {

    /* Метод добавляет задачи в историю просмотров.
       Помечает задачи как просмотренные.
    */

    void add(Task task);

    /* Метод для удаления задачи из просмотра
     */
    void remove(int id);

    /* Метод возвращает список задач.
       Список последних просмотренных задач в порядке их просмотра.
     */

    List<Task> getHistory();
}


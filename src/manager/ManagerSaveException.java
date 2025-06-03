package manager;

public class ManagerSaveException extends RuntimeException {

    // Создаем исключение с указанием сообщения и причины
    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerSaveException(String message) {
    }
}

package manager;

public final class Managers {
    // Создадим приватным конструктор для избежания создания экземпляра класса и наследования
    private Managers() {
    }

    // Метод возвращает реализацию TaskManager по умолчанию
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // Метод возвращает реализацию HistoryManager по умолчанию
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

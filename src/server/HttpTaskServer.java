package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HTTP-сервер для управления задачами.
 * Слушает порт 8080 и обрабатывает запросы.
 */
public class HttpTaskServer {
    private static final int PORT = 8080; // Порт сервера
    private final HttpServer server; // Встроенный HTTP-сервер
    private final TaskManager taskManager; // Менеджер задач
    private final Gson gson;

    /**
     * Конструктор сервера.
     * Инициализирует менеджер задач и настраивает обработчики.
     */
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault()); // Используем дефолтный менеджер
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager; // Инициализируем переданным менеджером
        this.gson = createGson(); // Инициализируем Gson
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0); // Создаём сервер

        // Регистрируем обработчики для каждого типа запросов
        server.createContext("/tasks", new TasksHandler(taskManager, gson)); // Обычные задачи
        server.createContext("/subtasks", new SubtasksHandler(taskManager, gson)); // Подзадачи
        server.createContext("/epics", new EpicsHandler(taskManager, gson)); // Эпики
        server.createContext("/history", new HistoryHandler(taskManager, gson)); // История
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson)); // Приоритетные задачи
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    /**
     * Метод запускает сервер.
     */
    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    /**
     * Метод останавливает сервер.
     */
    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    /**
     * Точка входа в программу.
     */
    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }
}

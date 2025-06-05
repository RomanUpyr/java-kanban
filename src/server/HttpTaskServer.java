package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * HTTP-сервер для управления задачами.
 * Слушает порт 8080 и обрабатывает запросы.
 */
public class HttpTaskServer {
    private static final int PORT = 8080; // Порт сервера
    private final HttpServer server; // Встроенный HTTP-сервер
    private final TaskManager taskManager; // Менеджер задач

    /**
     * Конструктор сервера.
     * Инициализирует менеджер задач и настраивает обработчики.
     */
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault()); // Используем дефолтный менеджер
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager; // Инициализируем переданным менеджером
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0); // Создаём сервер

        // Регистрируем обработчики для каждого типа запросов
        server.createContext("/tasks", new TasksHandler(taskManager)); // Обычные задачи
        server.createContext("/subtasks", new SubtasksHandler(taskManager)); // Подзадачи
        server.createContext("/epics", new EpicsHandler(taskManager)); // Эпики
        server.createContext("/history", new HistoryHandler(taskManager)); // История
        server.createContext("/prioritized", new PrioritizedHandler(taskManager)); // Приоритетные задачи
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

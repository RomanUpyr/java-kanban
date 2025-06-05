package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.GsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Базовый класс для всех HTTP-обработчиков.
 * Содержит общие методы для отправки ответов и парсинга JSON.
 */

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final Gson GSON = GsonUtils.getGson();

    // Стандартные HTTP-коды ответов
    protected static final int OK = 200; // Успешный запрос
    protected static final int CREATED = 201; // Успешное создание
    protected static final int NOT_FOUND = 404; // Ресурс не найден
    protected static final int NOT_ACCEPTABLE = 406; // Конфликт времени задач
    protected static final int INTERNAL_SERVER_ERROR = 500; // Ошибка сервера

    /**
     * Метод отправляет текстовый ответ клиенту.
     *
     * @param exchange   Объект HTTP-обмена
     * @param text       Текст ответа
     * @param statusCode HTTP-статус
     */
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Метод отправляет успешный ответ (200 OK).
     */
    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, OK);
    }

    /**
     * Метод отправляет ответ о создании ресурса (201 Created).
     */
    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, CREATED);
    }

    /**
     * Метод отправляет ошибку "Не найдено" (404 Not Found).
     */
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "Not Found", NOT_FOUND);
    }

    /**
     * Метод отправляет ошибку "Не принято" (406 Not Acceptable).
     * Используется при пересечении времени задач.
     */
    protected void sendNotAcceptable(HttpExchange exchange) throws IOException {
        sendText(exchange, "Task time overlaps with existing tasks", NOT_ACCEPTABLE);
    }

    /**
     * Метод отправляет ошибку сервера (500 Internal Server Error).
     */
    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendText(exchange, "Internal Server Error", INTERNAL_SERVER_ERROR);
    }

    /**
     * Метод парсит JSON из тела запроса в объект Java.
     *
     * @param inputStream Поток данных запроса
     * @param clazz       Класс, в который нужно преобразовать JSON
     * @return Объект типа T
     */
    protected <T> T parseJson(InputStream inputStream, Class<T> clazz) throws IOException {
        return GSON.fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), clazz);
    }

    public abstract void handle(HttpExchange exchange) throws IOException;
}

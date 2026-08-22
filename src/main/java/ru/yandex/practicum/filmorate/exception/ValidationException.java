package ru.yandex.practicum.filmorate.exception;

/**
 * Выбрасывается, если данные не прошли валидацию. Обрабатывается как 400 Bad Request.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

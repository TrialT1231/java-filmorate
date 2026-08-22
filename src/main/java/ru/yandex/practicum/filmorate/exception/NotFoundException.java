package ru.yandex.practicum.filmorate.exception;

/**
 * Выбрасывается, если запрошенный объект не найден. Обрабатывается как 404 Not Found.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

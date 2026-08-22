package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private static final String NAME_ERROR = "Название фильма не может быть пустым";
    private static final String DESCRIPTION_ERROR = "Максимальная длина описания — 200 символов";
    private static final String RELEASE_DATE_ERROR = "Дата релиза не может быть раньше 28 декабря 1895 года";
    private static final String DURATION_ERROR = "Продолжительность фильма должна быть положительным числом";

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Film name");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void shouldCreateValidFilmAndAssignId() {
        Film created = controller.create(validFilm());
        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        Film film = validFilm();
        film.setName(" ");
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(NAME_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        Film film = validFilm();
        film.setName(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(NAME_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDescriptionLongerThan200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(DESCRIPTION_ERROR, exception.getMessage());
    }

    @Test
    void shouldAllowDescriptionExactly200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldThrowWhenReleaseDateBeforeMinDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(RELEASE_DATE_ERROR, exception.getMessage());
    }

    @Test
    void shouldAllowReleaseDateEqualToMinDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldThrowWhenReleaseDateIsNull() {
        Film film = validFilm();
        film.setReleaseDate(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(RELEASE_DATE_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDurationIsNegative() {
        Film film = validFilm();
        film.setDuration(-10);
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(DURATION_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDurationIsZero() {
        Film film = validFilm();
        film.setDuration(0);
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals(DURATION_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenUpdatingFilmWithoutId() {
        Film film = validFilm();
        film.setId(null);
        assertThrows(ValidationException.class, () -> controller.update(film));
    }

    @Test
    void shouldThrowWhenUpdatingUnknownFilm() {
        Film film = validFilm();
        film.setId(999);
        assertThrows(NotFoundException.class, () -> controller.update(film));
    }
}
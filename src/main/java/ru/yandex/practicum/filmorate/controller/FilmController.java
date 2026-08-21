package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private int nextId = 1;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на список всех фильмов. Всего: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Не указан id фильма при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id={} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
        }
        validate(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм успешно обновлён: id={}, name={}", newFilm.getId(), newFilm.getName());
        return newFilm;
    }

    private void validate(Film film) {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            log.warn("Ошибка валидации фильма: {}", message);
            throw new ValidationException(message);
        }
    }

    private int getNextId() {
        return nextId++;
    }
}
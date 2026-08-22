package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return getFilmOrThrow(id);
    }

    public Film create(Film film) {
        Film created = filmStorage.create(film);
        log.info("Фильм успешно добавлен: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Не указан id фильма при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        getFilmOrThrow(film.getId());
        Film updated = filmStorage.update(film);
        log.info("Фильм успешно обновлён: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        checkUserExists(userId);
        film.getLikes().add(userId);
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        checkUserExists(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, filmId);
    }

    public List<Film> getPopular(Integer count) {
        int limit = (count == null || count <= 0) ? DEFAULT_POPULAR_COUNT : count;
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    private void checkUserExists(Integer id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}
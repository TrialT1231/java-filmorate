package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreService genreService,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return getFilmOrThrow(id);
    }

    public Film create(Film film) {
        checkMpaAndGenresExist(film);
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
        checkMpaAndGenresExist(film);
        Film updated = filmStorage.update(film);
        log.info("Фильм успешно обновлён: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    public void addLike(Integer filmId, Integer userId) {
        getFilmOrThrow(filmId);
        checkUserExists(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        getFilmOrThrow(filmId);
        checkUserExists(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, filmId);
    }

    public List<Film> getPopular(Integer count) {
        return filmStorage.getPopular(count);
    }

    private Film getFilmOrThrow(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    private void checkUserExists(Integer id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void checkMpaAndGenresExist(Film film) {
        mpaService.findById(film.getMpa().getId());

        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        genreService.checkAllExist(genreIds);
    }
}
package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    void delete(Integer id);

    Optional<Film> findById(Integer id);

    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    List<Film> getPopular(int count);
}
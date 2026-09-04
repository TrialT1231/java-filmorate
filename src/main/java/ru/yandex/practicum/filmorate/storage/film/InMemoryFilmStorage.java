package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Фильм сохранён в хранилище: id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        log.debug("Фильм обновлён в хранилище: id={}", film.getId());
        return film;
    }

    @Override
    public void delete(Integer id) {
        films.remove(id);
        log.debug("Фильм удалён из хранилища: id={}", id);
    }

    @Override
    public Optional<Film> findById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    private int getNextId() {
        return nextId++;
    }
}
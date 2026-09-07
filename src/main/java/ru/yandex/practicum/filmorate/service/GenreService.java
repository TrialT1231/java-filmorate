package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre findById(Integer id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }

    public void checkAllExist(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return;
        }

        Set<Integer> uniqueIds = new HashSet<>(ids);
        List<Genre> found = genreStorage.findAllByIds(uniqueIds);
        Set<Integer> foundIds = found.stream().map(Genre::getId).collect(Collectors.toSet());

        for (Integer id : uniqueIds) {
            if (!foundIds.contains(id)) {
                throw new NotFoundException("Жанр с id=" + id + " не найден");
            }
        }
    }
}
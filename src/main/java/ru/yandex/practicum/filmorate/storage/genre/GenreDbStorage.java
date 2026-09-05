package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT genre_id, name FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT genre_id, name FROM genres WHERE genre_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, new GenreRowMapper());
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        List<Genre> result = jdbcTemplate.query(FIND_BY_ID_QUERY, new GenreRowMapper(), id);
        return result.stream().findFirst();
    }
}
package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final String FIND_ALL_QUERY = "SELECT mpa_id, name FROM mpa ORDER BY mpa_id";
    private static final String FIND_BY_ID_QUERY = "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, new MpaRowMapper());
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        List<Mpa> result = jdbcTemplate.query(FIND_BY_ID_QUERY, new MpaRowMapper(), id);
        return result.stream().findFirst();
    }
}
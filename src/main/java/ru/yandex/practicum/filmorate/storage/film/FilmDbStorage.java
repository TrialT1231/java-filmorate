package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                    "FROM films f JOIN mpa m ON f.mpa_id = m.mpa_id";
    private static final String FIND_BY_ID_QUERY = FIND_ALL_QUERY + " WHERE f.film_id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY =
            "SELECT g.genre_id, g.name FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String FIND_GENRES_FOR_FILMS_QUERY_TEMPLATE =
            "SELECT fg.film_id, g.genre_id, g.name FROM film_genres fg " +
                    "JOIN genres g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id IN (%s) " +
                    "ORDER BY fg.film_id, g.genre_id";
    private static final String FIND_LIKES_BY_FILM_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String FIND_LIKES_FOR_FILMS_QUERY_TEMPLATE =
            "SELECT film_id, user_id FROM likes WHERE film_id IN (%s)";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String INSERT_FILM_GENRE_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String ADD_LIKE_QUERY =
            "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                    "COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY, new FilmRowMapper());
        enrichAll(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        updateGenres(film);
        film.setLikes(new HashSet<>());
        return film;
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        updateGenres(film);
        film.setLikes(new HashSet<>(jdbcTemplate.query(
                FIND_LIKES_BY_FILM_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId())));
        return film;
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<Film> findById(Integer id) {
        List<Film> result = jdbcTemplate.query(FIND_BY_ID_QUERY, new FilmRowMapper(), id);
        Optional<Film> film = result.stream().findFirst();
        film.ifPresent(this::enrichOne);
        return film;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        jdbcTemplate.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbcTemplate.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_QUERY, new FilmRowMapper(), count);
        enrichAll(films);
        return films;
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update(DELETE_FILM_GENRES_QUERY, film.getId());

        Set<Integer> genreIds = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            genreIds.add(genre.getId());
        }
        if (genreIds.isEmpty()) {
            return;
        }

        List<Integer> ids = new ArrayList<>(genreIds);
        jdbcTemplate.batchUpdate(INSERT_FILM_GENRE_QUERY, ids, ids.size(),
                (ps, genreId) -> {
                    ps.setInt(1, film.getId());
                    ps.setInt(2, genreId);
                });
    }

    private void enrichOne(Film film) {
        List<Genre> genres = jdbcTemplate.query(FIND_GENRES_BY_FILM_QUERY, new GenreRowMapper(), film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        List<Integer> likes = jdbcTemplate.query(
                FIND_LIKES_BY_FILM_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    // enrich для списков — два запроса, но только по тем film_id, что реально нужны
    private void enrichAll(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Integer> filmIds = films.stream().map(Film::getId).toList();
        String placeholders = String.join(", ", filmIds.stream().map(id -> "?").toList());

        Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();
        String genresQuery = String.format(FIND_GENRES_FOR_FILMS_QUERY_TEMPLATE, placeholders);
        jdbcTemplate.query(genresQuery, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            genresByFilmId.computeIfAbsent(filmId, id -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        Map<Integer, Set<Integer>> likesByFilmId = new HashMap<>();
        String likesQuery = String.format(FIND_LIKES_FOR_FILMS_QUERY_TEMPLATE, placeholders);
        jdbcTemplate.query(likesQuery, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            likesByFilmId.computeIfAbsent(filmId, id -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesByFilmId.getOrDefault(film.getId(), new HashSet<>()));
        }
    }
}
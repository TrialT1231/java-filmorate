package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film newFilm(String name, int mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        return film;
    }

    private Genre genre(int id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    private User newUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateAndFindFilmById() {
        Film created = filmStorage.create(newFilm("Film One", 1));

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Film One");
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    void shouldReturnEmptyOptionalWhenFilmNotFound() {
        assertThat(filmStorage.findById(9999)).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(newFilm("Film A", 1));
        filmStorage.create(newFilm("Film B", 2));

        assertThat(filmStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateFilm() {
        Film created = filmStorage.create(newFilm("Old name", 1));
        created.setName("New name");

        filmStorage.update(created);

        assertThat(filmStorage.findById(created.getId()))
                .hasValueSatisfying(film -> assertThat(film.getName()).isEqualTo("New name"));
    }

    @Test
    void shouldDeleteFilm() {
        Film created = filmStorage.create(newFilm("To delete", 1));

        filmStorage.delete(created.getId());

        assertThat(filmStorage.findById(created.getId())).isEmpty();
    }

    @Test
    void shouldSaveAndReturnGenres() {
        Film film = newFilm("With genres", 1);
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(genre(1));
        genres.add(genre(2));
        film.setGenres(genres);

        Film created = filmStorage.create(film);

        assertThat(created.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
    }

    @Test
    void shouldReplaceGenresOnUpdate() {
        Film film = newFilm("Genres update", 1);
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(genre(1));
        film.setGenres(genres);
        Film created = filmStorage.create(film);

        Set<Genre> newGenres = new LinkedHashSet<>();
        newGenres.add(genre(3));
        created.setGenres(newGenres);
        filmStorage.update(created);

        assertThat(filmStorage.findById(created.getId()))
                .hasValueSatisfying(f -> assertThat(f.getGenres()).extracting(Genre::getId).containsExactly(3));
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = filmStorage.create(newFilm("Likeable film", 1));
        User user = userStorage.create(newUser("liker@mail.ru", "liker"));

        filmStorage.addLike(film.getId(), user.getId());
        assertThat(filmStorage.findById(film.getId()))
                .hasValueSatisfying(f -> assertThat(f.getLikes()).containsExactly(user.getId()));

        filmStorage.removeLike(film.getId(), user.getId());
        assertThat(filmStorage.findById(film.getId()))
                .hasValueSatisfying(f -> assertThat(f.getLikes()).isEmpty());
    }

    @Test
    void shouldReturnPopularFilmsOrderedByLikes() {
        Film film1 = filmStorage.create(newFilm("Less popular", 1));
        Film film2 = filmStorage.create(newFilm("More popular", 1));
        User user1 = userStorage.create(newUser("u1@mail.ru", "u1"));
        User user2 = userStorage.create(newUser("u2@mail.ru", "u2"));

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(2);

        assertThat(popular).extracting(Film::getId).containsExactly(film2.getId(), film1.getId());
    }
}
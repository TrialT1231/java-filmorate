package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        assertThat(genreStorage.findAll()).hasSize(6);
    }

    @Test
    void shouldFindGenreById() {
        assertThat(genreStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(g -> assertThat(g.getName()).isEqualTo("Комедия"));
    }

    @Test
    void shouldReturnEmptyOptionalWhenGenreNotFound() {
        assertThat(genreStorage.findById(9999)).isEmpty();
    }
}
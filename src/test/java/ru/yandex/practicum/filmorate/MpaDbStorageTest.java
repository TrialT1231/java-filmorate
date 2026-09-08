package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;

    @Test
    void shouldFindAllMpaRatings() {
        assertThat(mpaStorage.findAll()).hasSize(5);
    }

    @Test
    void shouldFindMpaById() {
        assertThat(mpaStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(m -> assertThat(m.getName()).isEqualTo("G"));
    }

    @Test
    void shouldReturnEmptyOptionalWhenMpaNotFound() {
        assertThat(mpaStorage.findById(9999)).isEmpty();
    }
}
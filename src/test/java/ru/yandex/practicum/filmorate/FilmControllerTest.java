package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    private static final String NAME_ERROR = "Название фильма не может быть пустым";
    private static final String DESCRIPTION_ERROR = "Максимальная длина описания — 200 символов";
    private static final String RELEASE_DATE_ERROR = "Дата релиза не может быть раньше 28 декабря 1895 года";
    private static final String DURATION_ERROR = "Продолжительность фильма должна быть положительным числом";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film validFilm() {
        Film film = new Film();
        film.setName("Film name");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void shouldCreateValidFilm() throws Exception {
        Film film = validFilm();
        when(filmService.create(any(Film.class))).thenAnswer(invocation -> {
            Film saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        Film film = validFilm();
        film.setName(" ");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(NAME_ERROR));
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionTooLong() throws Exception {
        Film film = validFilm();
        film.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(DESCRIPTION_ERROR));
    }

    @Test
    void shouldReturnBadRequestWhenReleaseDateTooEarly() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(RELEASE_DATE_ERROR));
    }

    @Test
    void shouldReturnBadRequestWhenDurationIsNegative() throws Exception {
        Film film = validFilm();
        film.setDuration(-10);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(DURATION_ERROR));
    }

    @Test
    void shouldReturnNotFoundWhenFilmDoesNotExist() throws Exception {
        when(filmService.findById(anyInt()))
                .thenThrow(new NotFoundException("Фильм с id=999 не найден"));

        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с id=999 не найден"));
    }
}
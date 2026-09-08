package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @Test
    void shouldReturnAllGenres() throws Exception {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        when(genreService.findAll()).thenReturn(List.of(genre));

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"));
    }

    @Test
    void shouldReturnNotFoundWhenGenreDoesNotExist() throws Exception {
        when(genreService.findById(anyInt()))
                .thenThrow(new NotFoundException("Жанр с id=999 не найден"));

        mockMvc.perform(get("/genres/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Жанр с id=999 не найден"));
    }
}
package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MpaController.class)
class MpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MpaService mpaService;

    @Test
    void shouldReturnAllMpaRatings() throws Exception {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        when(mpaService.findAll()).thenReturn(List.of(mpa));

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"));
    }

    @Test
    void shouldReturnNotFoundWhenMpaDoesNotExist() throws Exception {
        when(mpaService.findById(anyInt()))
                .thenThrow(new NotFoundException("Рейтинг MPA с id=999 не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Рейтинг MPA с id=999 не найден"));
    }
}
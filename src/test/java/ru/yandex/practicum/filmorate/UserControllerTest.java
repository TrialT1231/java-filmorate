package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private static final String EMAIL_ERROR = "Электронная почта не может быть пустой и должна содержать символ @";
    private static final String LOGIN_ERROR = "Логин не может быть пустым и содержать пробелы";
    private static final String BIRTHDAY_ERROR = "Дата рождения не может быть в будущем";

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateValidUserAndAssignId() {
        User created = controller.create(validUser());
        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() {
        User user = validUser();
        user.setName(" ");
        User created = controller.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = validUser();
        user.setName(null);
        User created = controller.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void shouldThrowWhenEmailIsBlank() {
        User user = validUser();
        user.setEmail(" ");
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals(EMAIL_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenEmailHasNoAtSymbol() {
        User user = validUser();
        user.setEmail("user.example.com");
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals(EMAIL_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenLoginIsBlank() {
        User user = validUser();
        user.setLogin(" ");
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals(LOGIN_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenLoginContainsSpaces() {
        User user = validUser();
        user.setLogin("user login");
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals(LOGIN_ERROR, exception.getMessage());
    }

    @Test
    void shouldThrowWhenBirthdayIsInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals(BIRTHDAY_ERROR, exception.getMessage());
    }

    @Test
    void shouldAllowBirthdayToday() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> controller.create(user));
    }
}
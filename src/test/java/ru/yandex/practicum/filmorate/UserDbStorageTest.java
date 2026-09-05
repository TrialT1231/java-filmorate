package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    private User newUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login + " name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldCreateAndFindUserById() {
        User created = userStorage.create(newUser("first@mail.ru", "first"));

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(created.getId());
                    assertThat(user.getEmail()).isEqualTo("first@mail.ru");
                    assertThat(user.getLogin()).isEqualTo("first");
                });
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        assertThat(userStorage.findById(9999)).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(newUser("a@mail.ru", "a_login"));
        userStorage.create(newUser("b@mail.ru", "b_login"));

        assertThat(userStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateUser() {
        User created = userStorage.create(newUser("update@mail.ru", "update_login"));
        created.setName("Updated Name");

        userStorage.update(created);

        assertThat(userStorage.findById(created.getId()))
                .hasValueSatisfying(user -> assertThat(user.getName()).isEqualTo("Updated Name"));
    }

    @Test
    void shouldDeleteUser() {
        User created = userStorage.create(newUser("delete@mail.ru", "delete_login"));

        userStorage.delete(created.getId());

        assertThat(userStorage.findById(created.getId())).isEmpty();
    }

    @Test
    void shouldAddAndGetFriends() {
        User user = userStorage.create(newUser("user@mail.ru", "user_login"));
        User friend = userStorage.create(newUser("friend@mail.ru", "friend_login"));

        userStorage.addFriend(user.getId(), friend.getId());
        List<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends).extracting(User::getId).containsExactly(friend.getId());
    }

    @Test
    void shouldNotAddFriendBackAutomatically() {
        User user = userStorage.create(newUser("one@mail.ru", "one_login"));
        User friend = userStorage.create(newUser("two@mail.ru", "two_login"));

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void shouldRemoveFriend() {
        User user = userStorage.create(newUser("rem_user@mail.ru", "rem_user_login"));
        User friend = userStorage.create(newUser("rem_friend@mail.ru", "rem_friend_login"));
        userStorage.addFriend(user.getId(), friend.getId());

        userStorage.removeFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User user = userStorage.create(newUser("common_user@mail.ru", "common_user_login"));
        User other = userStorage.create(newUser("common_other@mail.ru", "common_other_login"));
        User commonFriend = userStorage.create(newUser("common_friend@mail.ru", "common_friend_login"));

        userStorage.addFriend(user.getId(), commonFriend.getId());
        userStorage.addFriend(other.getId(), commonFriend.getId());

        assertThat(userStorage.getCommonFriends(user.getId(), other.getId()))
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }
}
package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    void delete(Integer id);

    Optional<User> findById(Integer id);

    void addFriend(Integer userId, Integer friendId);

    void removeFriend(Integer userId, Integer friendId);

    List<User> getFriends(Integer userId);

    List<User> getCommonFriends(Integer userId, Integer otherId);
}
package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friendships = new HashMap<>();
    private int nextId = 1;

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь сохранён в хранилище: id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.debug("Пользователь обновлён в хранилище: id={}", user.getId());
        return user;
    }

    @Override
    public void delete(Integer id) {
        users.remove(id);
        friendships.remove(id);
        log.debug("Пользователь удалён из хранилища: id={}", id);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        friendships.computeIfAbsent(userId, id -> new HashSet<>()).add(friendId);
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        Set<Integer> friends = friendships.get(userId);
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(Integer userId) {
        return friendships.getOrDefault(userId, Set.of()).stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        Set<Integer> common = new HashSet<>(friendships.getOrDefault(userId, Set.of()));
        common.retainAll(friendships.getOrDefault(otherId, Set.of()));
        return common.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private int getNextId() {
        return nextId++;
    }
}
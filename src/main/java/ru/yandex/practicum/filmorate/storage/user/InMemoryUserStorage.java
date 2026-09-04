package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
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
        log.debug("Пользователь удалён из хранилища: id={}", id);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    private int getNextId() {
        return nextId++;
    }
}
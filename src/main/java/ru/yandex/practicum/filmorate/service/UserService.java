package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return getUserOrThrow(id);
    }

    public User create(User user) {
        applyNameFallback(user);
        User created = userStorage.create(user);
        log.info("Пользователь успешно создан: id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Не указан id пользователя при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        getUserOrThrow(user.getId());
        applyNameFallback(user);
        User updated = userStorage.update(user);
        log.info("Пользователь успешно обновлён: id={}, login={}", updated.getId(), updated.getLogin());
        return updated;
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (friend.getFriends().containsKey(userId)) {
            // friend уже отправлял запрос пользователю user — теперь дружба подтверждается с обеих сторон
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
            log.info("Дружба между id={} и id={} подтверждена", userId, friendId);
        } else {
            // это первый запрос — дружба остаётся неподтверждённой до ответа friend
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
            log.info("Пользователь id={} отправил запрос на дружбу пользователю id={}", userId, friendId);
        }
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        FriendshipStatus previousStatus = user.getFriends().remove(friendId);
        if (previousStatus == FriendshipStatus.CONFIRMED) {
            // если дружба была взаимной, у второй стороны она превращается обратно в неподтверждённую заявку
            friend.getFriends().put(userId, FriendshipStatus.UNCONFIRMED);
        }
        log.info("Пользователь id={} удалил из друзей id={}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        User user = getUserOrThrow(userId);
        return user.getFriends().keySet().stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);
        Set<Integer> common = new HashSet<>(user.getFriends().keySet());
        common.retainAll(other.getFriends().keySet());
        return common.stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void applyNameFallback(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final String FIND_ALL_QUERY =
            "SELECT user_id, email, login, name, birthday FROM users";
    private static final String FIND_BY_ID_QUERY = FIND_ALL_QUERY + " WHERE user_id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY =
            "MERGE INTO friendship (user_id, friend_id, status) KEY (user_id, friend_id) VALUES (?, ?, ?)";
    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                    "FROM friendship fr JOIN users u ON u.user_id = fr.friend_id " +
                    "WHERE fr.user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                    "FROM friendship fr1 " +
                    "JOIN friendship fr2 ON fr1.friend_id = fr2.friend_id " +
                    "JOIN users u ON u.user_id = fr1.friend_id " +
                    "WHERE fr1.user_id = ? AND fr2.user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, new UserRowMapper());
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<User> findById(Integer id) {
        List<User> result = jdbcTemplate.query(FIND_BY_ID_QUERY, new UserRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        jdbcTemplate.update(ADD_FRIEND_QUERY, userId, friendId, FriendshipStatus.CONFIRMED.name());
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        jdbcTemplate.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        return jdbcTemplate.query(FIND_FRIENDS_QUERY, new UserRowMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        return jdbcTemplate.query(FIND_COMMON_FRIENDS_QUERY, new UserRowMapper(), userId, otherId);
    }
}
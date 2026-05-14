package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository("jdbcUserStorage")
public class UserRepository extends BaseRepository<User> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT u.* FROM users u INNER JOIN friends f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN friends f1 ON u.id = f1.friend_id " +
                    "JOIN friends f2 ON u.id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ?";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findById(long userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public User save(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    public boolean delete(long id) {
        return delete(DELETE_QUERY, id);
    }

    public void addFriend(long userId, long friendId) {
        String checkQuery = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(checkQuery, Integer.class, userId, friendId);
        if (count == 0) {
            update(ADD_FRIEND_QUERY, userId, friendId);
        }
    }

    public void deleteFriend(long userId, long friendId) {
        // Удаляем без дополнительной проверки, так как проверка уже есть в сервисе
        int rowsDeleted = jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
        // Не выбрасываем исключение здесь, так как проверка уже была в сервисе
    }

    public List<User> findFriends(long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

    public List<User> findCommonFriends(long userId, long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId);
    }
}
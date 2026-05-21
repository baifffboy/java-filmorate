package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> findAll();

    Optional<User> findById(long userId);

    Optional<User> findByEmail(String email);

    User save(User user);

    User update(User user);

    boolean delete(long id);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    List<User> findFriends(long userId);

    List<User> findCommonFriends(long userId, long otherId);
}

package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    boolean containsKey(Long id);

    long getNextId();

    User getUserById(Long id);

    Collection<User> getAllValues();

    User addUser(Long id, User user);

    User deleteUser(Long id, Long whoIsDeleted);

    User updateUser(User user);
}

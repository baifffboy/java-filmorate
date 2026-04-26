package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
public class UserService {

    private UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public boolean containsKey(Long id) {
        return userStorage.containsKey(id);
    }

    public long getNextIdFromStorage() {
        return userStorage.getNextId();
    }

    public User getUserByIdFromStorage(Long id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> getAllUsersFromStorage() {
        return userStorage.getAllValues();
    }

    public Long addUserInStorage(Long id, User user) {
        return userStorage.addUser(id, user);
    }

    public Long deleteUserInStorage(Long id, Long whoIsDeleted) {
        return userStorage.deleteUser(id, whoIsDeleted);
    }
}

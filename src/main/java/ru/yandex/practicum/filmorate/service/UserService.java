package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public boolean containsKey(Long id) {
        return userStorage.containsKey(id);
    }

    public User getUserByIdFromStorage(Long id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> getAllUsersFromStorage() {
        return userStorage.getAllValues();
    }

    public User addUserInStorage(User user) {
        user.setId(userStorage.getNextId());
        return userStorage.addUser(user.getId(), user);
    }

    public User deleteUserInStorage(Long id, Long whoIsDeleted) {
        return userStorage.deleteUser(id, whoIsDeleted);
    }

    public User updateUserInStorage(User user) {
        return userStorage.updateUser(user);
    }
}

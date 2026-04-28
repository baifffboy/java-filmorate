package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        User user = userStorage.getUserById(id);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return user;
    }

    public Collection<User> getAllUsersFromStorage() {
        return userStorage.getAllValues();
    }

    public User addUserInStorage(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(userStorage.getNextId());
        return userStorage.addUser(user.getId(), user);
    }

    public Collection<User> deleteFriendInStorage(Long id, Long friendId) {
        if (!this.containsKey(id) || !this.containsKey(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return userStorage.deleteFriend(id, friendId);
    }

    public User updateUserInStorage(User user) {
        if (user.getId() == null) {
            log.error("Ошибка порядкового номера(id) пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        if (!this.containsKey(user.getId())) {
            log.error("Ошибка существования пользователя");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост с id = " + user.getId() + " не найден");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        return userStorage.updateUser(user);
    }

    public Collection<User> addFriendInStorage(Long id, Long friendId) {
        if (!this.containsKey(id) || !this.containsKey(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return userStorage.addFriend(id, friendId);
    }

    public Collection<User> getFriends(Long id) {
        if (!this.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return this.getUserByIdFromStorage(id).getFriends().stream()
                .map(this::getUserByIdFromStorage)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return getUserByIdFromStorage(id).getFriends().stream()
                .filter(this.getUserByIdFromStorage(otherId).getFriends()::contains)
                .map(this::getUserByIdFromStorage)
                .collect(Collectors.toSet());
    }
}

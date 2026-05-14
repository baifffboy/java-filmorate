package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("jdbcUserStorage") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByIdFromStorage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));

        List<User> friends = userRepository.findFriends(id);
        user.getFriends().clear();
        friends.forEach(friend -> user.getFriends().add(friend.getId()));

        return user;
    }

    public Collection<User> getAllUsersFromStorage() {
        return userRepository.findAll();
    }

    public User addUserInStorage(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userRepository.save(user);
    }

    public User updateUserInStorage(User user) {
        if (user.getId() == null) {
            log.error("Ошибка порядкового номера(id) пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user.getId() + " не найден"));

        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userRepository.update(user);
    }

    public Collection<User> addFriendInStorage(Long id, Long friendId) {
        getUserByIdFromStorage(id);
        getUserByIdFromStorage(friendId);
        userRepository.addFriend(id, friendId);
        return getFriends(id);
    }

    public Collection<User> deleteFriendInStorage(Long id, Long friendId) {
        // Проверяем, существуют ли пользователи
        User user = getUserByIdFromStorage(id);
        User friend = getUserByIdFromStorage(friendId);

        // Проверяем, есть ли дружба (ВНИМАНИЕ: проверка должна быть ДО вызова репозитория)
        if (!user.getFriends().contains(friendId)) {
            throw new NotFoundException("Пользователи " + id + " и " + friendId + " не являются друзьями");
        }

        userRepository.deleteFriend(id, friendId);
        user.getFriends().remove(friendId);
        return getFriends(id);
    }

    public Collection<User> getFriends(Long id) {
        getUserByIdFromStorage(id);
        return userRepository.findFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        getUserByIdFromStorage(id);
        getUserByIdFromStorage(otherId);
        return userRepository.findCommonFriends(id, otherId);
    }
}
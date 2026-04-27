package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        User user = userService.getUserByIdFromStorage(id);
        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Collection<User> addFriend(@PathVariable Long id,
                                      @PathVariable Long friendId) {
        if (!userService.containsKey(id) || !userService.containsKey(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        User user = userService.getUserByIdFromStorage(id);
        User friend = userService.getUserByIdFromStorage(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        return List.of(user, friend);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Collection<User> deleteFriend(@PathVariable Long id,
                                         @PathVariable Long friendId) {

        if (!userService.containsKey(id) || !userService.containsKey(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        User deletedFriendFromId = userService.deleteUserInStorage(userService.getUserByIdFromStorage(id).getId(), userService.getUserByIdFromStorage(friendId).getId());
        User deletedIdFromFriend = userService.deleteUserInStorage(userService.getUserByIdFromStorage(friendId).getId(), userService.getUserByIdFromStorage(id).getId());

        return List.of(deletedFriendFromId, deletedIdFromFriend);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        if (!userService.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Set<Long> idOfFriendUsers = userService.getUserByIdFromStorage(id).getFriends();
        return idOfFriendUsers.stream()
                .map(userService::getUserByIdFromStorage)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> commonFriend(@PathVariable Long id,
                                         @PathVariable Long otherId) {
        if (!userService.containsKey(id) || !userService.containsKey(otherId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return userService.getUserByIdFromStorage(id).getFriends().stream()
                .filter(userService.getUserByIdFromStorage(otherId).getFriends()::contains)
                .map(userService::getUserByIdFromStorage)
                .collect(Collectors.toSet());
    }

    @PostMapping
    public User postUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Создание пользователя: {}", user);
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        User postUser = userService.addUserInStorage(user);
        log.info("Пользователь создан с id={}", postUser.getId());
        return postUser;
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Пользователь редактирует пользователя " + user.toString());
        if (user.getId() == null) {
            log.error("Ошибка порядкового номера(id) пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        if (!userService.containsKey(user.getId())) {
            log.error("Ошибка существования пользователя");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,  "Пост с id = " + user.getId() + " не найден");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        return userService.updateUserInStorage(user);
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Пользователь запросил все пользователей");
        return userService.getAllUsersFromStorage();
    }

}

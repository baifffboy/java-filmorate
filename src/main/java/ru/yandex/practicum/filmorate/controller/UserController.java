package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

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
        return userService.getUserByIdFromStorage(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Collection<User> addFriend(@PathVariable Long id,
                                      @PathVariable Long friendId) {
        return userService.addFriendInStorage(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Collection<User> deleteFriend(@PathVariable Long id,
                                         @PathVariable Long friendId) {
        return userService.deleteFriendInStorage(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> commonFriend(@PathVariable Long id,
                                         @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User postUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Создание пользователя: {}", user);
        return userService.addUserInStorage(user);
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Пользователь редактирует пользователя " + user.toString());
        return userService.updateUserInStorage(user);
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Пользователь запросил все пользователей");
        return userService.getAllUsersFromStorage();
    }

}

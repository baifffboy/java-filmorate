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
        return userService.getUserByIdFromStorage(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Set<Long> addFriend(@PathVariable Long id,
                               @PathVariable Long friendId) {
        if (!userService.containsKey(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!userService.containsKey(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        User user = userService.getUserByIdFromStorage(id);
        User friend = userService.getUserByIdFromStorage(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        return Set.of(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Set<User> deleteFriend(@PathVariable Long id,
                                  @PathVariable Long friendId) {
        User deletedFriendFromId;
        User deletedIdFromFriend;
        if (userService.containsKey(id) && userService.containsKey(friendId)) {
            deletedFriendFromId = userService.deleteUserInStorage(userService.getUserByIdFromStorage(id).getId(), userService.getUserByIdFromStorage(friendId).getId());
            deletedIdFromFriend = userService.deleteUserInStorage(userService.getUserByIdFromStorage(friendId).getId(), userService.getUserByIdFromStorage(id).getId());
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Set.of(deletedFriendFromId, deletedIdFromFriend);
    }

    @GetMapping("/{id}/friends")
    public Set<Long> getFriends(@PathVariable Long id) {
        if (userService.containsKey(id)) return userService.getUserByIdFromStorage(id).getFriends();
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<Long> commonFriend(@PathVariable Long id,
                                  @PathVariable Long otherId) {
        return userService.getUserByIdFromStorage(id).getFriends().stream()
                .filter(userService.getUserByIdFromStorage(otherId).getFriends()::contains)
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
        User updatedUser;
        if (userService.containsKey(user.getId())) {
            if (user.getLogin().contains(" ")) {
                log.error("Логин не может содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            updatedUser = userService.updateUserInStorage(user);
        } else {
            log.error("Ошибка существования пользователя");
            throw new ValidationException("Пост с id = " + user.getId() + " не найден");
        }
        return updatedUser;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Пользователь запросил все пользователей");
        return userService.getAllUsersFromStorage();
    }

}

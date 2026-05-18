package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.users.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.users.UserResponse;
import ru.yandex.practicum.filmorate.dto.users.UserUpdateRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
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
    public UserResponse getUserById(@PathVariable Long id) {
        log.info("Пользователь запросил человека с id={}", id);
        return UserMapper.toUserResponse(userService.getUserByIdFromStorage(id));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Collection<UserResponse> addFriend(@PathVariable Long id,
                                              @PathVariable Long friendId) {
        log.info("Пользователь добавил друга с id={} у id={}", friendId, id);
        Collection<User> friends = userService.addFriendInStorage(id, friendId);
        return friends.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Collection<UserResponse> deleteFriend(@PathVariable Long id,
                                                 @PathVariable Long friendId) {
        log.info("Пользователь удалил друга с id={} у id={}", friendId, id);
        Collection<User> friends = userService.deleteFriendInStorage(id, friendId);
        return friends.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());

    }

    @GetMapping("/{id}/friends")
    public Collection<UserResponse> getFriends(@PathVariable Long id) {
        log.info("Пользователь запросил друзей id={}", id);
        Collection<User> friends = userService.getFriends(id);
        return friends.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserResponse> commonFriend(@PathVariable Long id,
                                                 @PathVariable Long otherId) {
        log.info("Пользователь запросил общих друзей id={} и id={}", id, otherId);
        Collection<User> commonFriends = userService.getCommonFriends(id, otherId);
        return commonFriends.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserResponse postUser(@Valid @RequestBody UserCreateRequest user) throws ValidationException {
        log.info("Создание пользователя: {}", user);
        return UserMapper.toUserResponse(userService.addUserInStorage(user));
    }

    @PutMapping
    public UserResponse putUser(@Valid @RequestBody UserUpdateRequest user) throws ValidationException {
        log.info("Пользователь редактирует пользователя " + user.toString());
        return UserMapper.toUserResponse(userService.updateUserInStorage(user));
    }

    @GetMapping
    public Collection<UserResponse> getAllUsers() {
        log.info("Пользователь запросил всех пользователей");
        Collection<User> users = userService.getAllUsersFromStorage();
        return users.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }
}
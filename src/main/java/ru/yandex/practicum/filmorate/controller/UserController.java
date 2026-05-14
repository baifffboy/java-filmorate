package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getUserByIdFromStorage(id);
        return UserMapper.toDto(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Collection<UserDto> addFriend(@PathVariable Long id,
                                         @PathVariable Long friendId) {
        Collection<User> friends = userService.addFriendInStorage(id, friendId);
        return friends.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Collection<UserDto> deleteFriend(@PathVariable Long id,
                                            @PathVariable Long friendId) {
        try {
            Collection<User> friends = userService.deleteFriendInStorage(id, friendId);
            return friends.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getFriends(@PathVariable Long id) {
        Collection<User> friends = userService.getFriends(id);
        return friends.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> commonFriend(@PathVariable Long id,
                                            @PathVariable Long otherId) {
        Collection<User> commonFriends = userService.getCommonFriends(id, otherId);
        return commonFriends.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserDto postUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Создание пользователя: {}", user);
        User createdUser = userService.addUserInStorage(user);
        return UserMapper.toDto(createdUser);
    }

    @PutMapping
    public UserDto putUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Пользователь редактирует пользователя " + user.toString());
        User updatedUser = userService.updateUserInStorage(user);
        return UserMapper.toDto(updatedUser);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        log.info("Пользователь запросил всех пользователей");
        Collection<User> users = userService.getAllUsersFromStorage();
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }
}
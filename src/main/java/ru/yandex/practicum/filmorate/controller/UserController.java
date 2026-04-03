package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User postUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Создание пользователя: {}", user);
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан с id={}", user.getId());
        return user;
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Пользователь редактирует пользователя " + user.toString());
        if (user.getId() == null) {
            log.error("Ошибка порядкового номера(id) пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        User oldUser;
        if (users.containsKey(user.getId())) {
            if (user.getLogin().contains(" ")) {
                log.error("Логин не может содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            oldUser = users.get(user.getId());
            oldUser.setEmail(user.getEmail());
            oldUser.setLogin(user.getLogin());
            if (user.getName() == null || user.getName().isBlank()) oldUser.setName(oldUser.getLogin());
            else oldUser.setName(user.getName());
            oldUser.setBirthday(user.getBirthday());
        } else {
            log.error("Ошибка существования пользователя");
            throw new ValidationException("Пост с id = " + user.getId() + " не найден");
        }
        return oldUser;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Пользователь запросил все пользователей");
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

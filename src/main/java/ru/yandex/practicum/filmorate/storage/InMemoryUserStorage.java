package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public boolean containsKey(Long id) {
        return users.containsKey(id);
    }

    public long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    public User save(User user) {
        long id = getNextId();
        users.put(id, user);
        log.info("Пользователь создан с id={}", id);
        return users.get(id);
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        users.get(id).getFriends().remove(friendId);
        users.get(friendId).getFriends().remove(id);
    }

    @Override
    public User update(User user) {
        User existingUser = users.get(user.getId());
        if (existingUser == null) return null;
        Set<Long> existingFriends = existingUser.getFriends();
        user.setFriends(existingFriends);
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        users.get(userId).getFriends().add(friendId);
        users.get(friendId).getFriends().add(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public boolean delete(long id) {
        long s = users.size();
        users.remove(id);
        return s == users.size() + 1;
    }

    @Override
    public List<User> findFriends(long userId) {
        return users.get(userId).getFriends().stream().map(id -> findById(id).get()).toList();
    }

    @Override
    public List<User> findCommonFriends(long userId, long otherId) {
        return users.get(userId).getFriends().stream()
                .filter(users.get(otherId).getFriends()::contains)
                .map(this::findById)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public boolean containsKey(Long id) {
        return users.containsKey(id);
    }

    @Override
    public long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public User getUserById(Long id) {
        return users.get(id);
    }

    @Override
    public Collection<User> getAllValues() {
        return users.values();
    }

    @Override
    public User addUser(Long id, User user) {
        users.put(id, user);
        log.info("Пользователь создан с id={}", id);
        return users.get(id);
    }

    @Override
    public Collection<User> deleteFriend(Long id, Long friendId) {
        users.get(id).getFriends().remove(friendId);
        users.get(friendId).getFriends().remove(id);
        return List.of(users.get(id), users.get(friendId));
    }

    @Override
    public User updateUser(User user) {
        User existingUser = users.get(user.getId());
        if (existingUser == null) return null;
        Set<Long> existingFriends = existingUser.getFriends();
        user.setFriends(existingFriends);
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> addFriend(Long id, Long friendId) {
        users.get(id).getFriends().add(friendId);
        users.get(friendId).getFriends().add(id);
        return List.of(users.get(id), users.get(friendId));
    }
}
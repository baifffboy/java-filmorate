package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
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
    public Long addUser(Long id, User user) {
        users.put(id, user);
        return id;
    }

    @Override
    public Long deleteUser(Long id, Long whoIsDeleted) {
        users.get(id).getFriends().remove(whoIsDeleted);
        return id;
    }
}

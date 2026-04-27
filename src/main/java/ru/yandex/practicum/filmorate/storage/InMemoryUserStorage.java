package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public User addUser(Long id, User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(id, user);
        return users.get(id);
    }

    @Override
    public User deleteUser(Long id, Long whoIsDeleted) {
        User deletedUser = users.get(id);
        users.get(id).getFriends().remove(whoIsDeleted);
        return deletedUser;
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
}

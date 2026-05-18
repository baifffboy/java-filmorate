package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.users.UserResponse;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        dto.setFriends(user.getFriends());
        return dto;
    }
}

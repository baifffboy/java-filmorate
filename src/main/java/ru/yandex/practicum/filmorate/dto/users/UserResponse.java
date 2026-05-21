package ru.yandex.practicum.filmorate.dto.users;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friends;
}
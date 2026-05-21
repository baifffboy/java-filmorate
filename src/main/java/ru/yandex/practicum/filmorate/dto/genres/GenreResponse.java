package ru.yandex.practicum.filmorate.dto.genres;

import lombok.Data;

@Data
public class GenreResponse {
    private Integer id;
    private String name;

    public GenreResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
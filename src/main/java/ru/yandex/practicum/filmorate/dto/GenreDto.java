package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class GenreDto {
    private Integer id;
    private String name;

    public GenreDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class MpaDto {
    private Integer id;
    private String name;

    public MpaDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
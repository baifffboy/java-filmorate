package ru.yandex.practicum.filmorate.dto.mpa;

import lombok.Data;

@Data
public class MpaResponse {
    private Integer id;
    private String name;

    public MpaResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
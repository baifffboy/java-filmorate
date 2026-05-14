package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;           // ← Поле для MPA
    private Set<GenreDto> genres; // ← Поле для жанров
    private Integer likesCount;
}
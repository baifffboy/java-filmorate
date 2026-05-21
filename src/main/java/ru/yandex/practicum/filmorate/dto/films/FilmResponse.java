package ru.yandex.practicum.filmorate.dto.films;

import lombok.Data;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaResponse mpa;
    private Set<GenreResponse> genres;
    private Integer likesCount;
}
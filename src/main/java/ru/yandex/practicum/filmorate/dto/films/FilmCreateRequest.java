package ru.yandex.practicum.filmorate.dto.films;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.genres.GenreRequest;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRequest;

import java.time.LocalDate;
import java.util.List;

@Data
public class FilmCreateRequest {
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть null")
    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    private MpaRequest mpa;

    private List<GenreRequest> genres;
}
package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Arrays;
import java.util.List;

@Service
public class GenreService {

    public List<GenreDto> getAllGenres() {
        return Arrays.asList(
                new GenreDto(1, "Комедия"),
                new GenreDto(2, "Драма"),
                new GenreDto(3, "Мультфильм"),
                new GenreDto(4, "Триллер"),
                new GenreDto(5, "Документальный"),
                new GenreDto(6, "Боевик")
        );
    }

    public GenreDto getGenreById(int id) {
        switch (id) {
            case 1:
                return new GenreDto(1, "Комедия");
            case 2:
                return new GenreDto(2, "Драма");
            case 3:
                return new GenreDto(3, "Мультфильм");
            case 4:
                return new GenreDto(4, "Триллер");
            case 5:
                return new GenreDto(5, "Документальный");
            case 6:
                return new GenreDto(6, "Боевик");
            default:
                throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }
}

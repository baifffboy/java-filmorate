package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Arrays;
import java.util.List;

@Service
public class GenreService {

    public List<GenreResponse> getAllGenres() {
        return Arrays.asList(
                new GenreResponse(1, "Комедия"),
                new GenreResponse(2, "Драма"),
                new GenreResponse(3, "Мультфильм"),
                new GenreResponse(4, "Триллер"),
                new GenreResponse(5, "Документальный"),
                new GenreResponse(6, "Боевик")
        );
    }

    public GenreResponse getGenreById(int id) {
        switch (id) {
            case 1:
                return new GenreResponse(1, "Комедия");
            case 2:
                return new GenreResponse(2, "Драма");
            case 3:
                return new GenreResponse(3, "Мультфильм");
            case 4:
                return new GenreResponse(4, "Триллер");
            case 5:
                return new GenreResponse(5, "Документальный");
            case 6:
                return new GenreResponse(6, "Боевик");
            default:
                throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }
}

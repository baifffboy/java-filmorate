package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;

public class GenreMapper {
    public static Integer mapGenreToId(GenreOfFilm genre) {
        if (genre == null) return null;
        switch (genre) {
            case COMEDY:
                return 1;
            case DRAMA:
                return 2;
            case CARTOON:
                return 3;
            case THRILLER:
                return 4;
            case DOCUMENTARY:
                return 5;
            case ACTION:
                return 6;
            default:
                throw new NotFoundException("Данного жанра нет в списке!");
        }
    }

    public static GenreOfFilm mapIdToGenre(Integer id) {
        if (id == null) return null;
        switch (id) {
            case 1:
                return GenreOfFilm.COMEDY;
            case 2:
                return GenreOfFilm.DRAMA;
            case 3:
                return GenreOfFilm.CARTOON;
            case 4:
                return GenreOfFilm.THRILLER;
            case 5:
                return GenreOfFilm.DOCUMENTARY;
            case 6:
                return GenreOfFilm.ACTION;
            default:
                throw new NotFoundException("Данного жанра нет в списке!");
        }
    }

    public static GenreOfFilm mapStringToGenre(String genreName) {
        if (genreName == null) return null;
        switch (genreName) {
            case "Комедия":
                return GenreOfFilm.COMEDY;
            case "Драма":
                return GenreOfFilm.DRAMA;
            case "Мультфильм":
                return GenreOfFilm.CARTOON;
            case "Триллер":
                return GenreOfFilm.THRILLER;
            case "Документальный":
                return GenreOfFilm.DOCUMENTARY;
            case "Боевик":
                return GenreOfFilm.ACTION;
            default:
                throw new NotFoundException("Данного жанра нет в списке!");
        }
    }

    public static GenreResponse mapGenretoGenreResponse(Integer genreId) {
        return new GenreResponse(genreId, mapGenreByIdToString(genreId));
    }

    public static String mapGenreByIdToString(Integer genreId) {
        switch (genreId) {
            case 1:
                return "Комедия";
            case 2:
                return "Драма";
            case 3:
                return "Мультфильм";
            case 4:
                return "Триллер";
            case 5:
                return "Документальный";
            case 6:
                return "Боевик";
            default:
                throw new NotFoundException("Данного жанра нет в списке!");
        }
    }
}

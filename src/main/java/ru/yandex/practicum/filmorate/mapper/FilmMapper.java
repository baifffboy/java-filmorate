package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

public class FilmMapper {

    public static FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setGenres(film.getGenres());  // Изменено: getGenres, не getGenre
        dto.setMpa(film.getMpa());
        if (film.getIdOfUsersWhoLikedThisFilm() != null) {
            dto.setLikesCount(film.getIdOfUsersWhoLikedThisFilm().size());
        }
        return dto;
    }

    public static Film toEntity(FilmDto dto) {
        if (dto == null) {
            return null;
        }
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setGenres(dto.getGenres());  // Изменено: setGenres, не setGenre
        film.setMpa(dto.getMpa());
        return film;
    }
}
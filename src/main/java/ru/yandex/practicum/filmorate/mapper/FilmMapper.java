package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.films.FilmResponse;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;

import java.util.LinkedHashSet;
import java.util.Set;

public class FilmMapper {
    public static FilmResponse toFilmResponse(Film film) {
        if (film == null) return null;

        FilmResponse dto = new FilmResponse();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getIdOfUsersWhoLikedThisFilm() != null) {
            dto.setLikesCount(film.getIdOfUsersWhoLikedThisFilm().size());
        } else {
            dto.setLikesCount(0);
        }

        if (film.getMpa() != null) {
            Integer mpaId = MapMapper.mapMpaToId(film.getMpa());
            MpaResponse MpaResponse = MapMapper.mapMpatoMpaResponse(mpaId);
            dto.setMpa(MpaResponse);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<GenreResponse> GenreResponses = new LinkedHashSet<>();
            for (GenreOfFilm genre : film.getGenres()) {
                Integer genreId = GenreMapper.mapGenreToId(genre);
                GenreResponse GenreResponse = GenreMapper.mapGenretoGenreResponse(genreId);
                GenreResponses.add(GenreResponse);
            }
            dto.setGenres(GenreResponses);
        }

        return dto;
    }
}
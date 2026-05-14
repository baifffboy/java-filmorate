package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.LinkedHashSet;
import java.util.Set;

public class FilmMapper {

    private static MpaService mpaService;
    private static GenreService genreService;

    public static void initServices(MpaService mpaService, GenreService genreService) {
        FilmMapper.mpaService = mpaService;
        FilmMapper.genreService = genreService;
    }

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

        if (film.getMpa() != null && mpaService != null) {
            Integer mpaId = mapMpaToId(film.getMpa());
            MpaDto mpaDto = mpaService.getMpaById(mpaId);
            dto.setMpa(mpaDto);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<GenreDto> genreDtos = new LinkedHashSet<>();
            for (GenreOfFilm genre : film.getGenres()) {
                if (genreService != null) {
                    Integer genreId = mapGenreToId(genre);
                    GenreDto genreDto = genreService.getGenreById(genreId);
                    genreDtos.add(genreDto);
                }
            }
            dto.setGenres(genreDtos);
        }

        if (film.getIdOfUsersWhoLikedThisFilm() != null) {
            dto.setLikesCount(film.getIdOfUsersWhoLikedThisFilm().size());
        }

        return dto;
    }

    private static Integer mapMpaToId(MotionPictureAssociation mpa) {
        if (mpa == null) return null;
        switch (mpa) {
            case G:
                return 1;
            case PG:
                return 2;
            case PG13:
                return 3;
            case R:
                return 4;
            case NC17:
                return 5;
            default:
                return null;
        }
    }

    private static Integer mapGenreToId(GenreOfFilm genre) {
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
                return null;
        }
    }
}
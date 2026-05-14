package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public FilmController(FilmService filmService, MpaService mpaService, GenreService genreService) {
        this.filmService = filmService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmByIdFromStorage(id);
        return convertToFilmDto(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.postLikeOnFilmInStorage(id, userId);
        return convertToFilmDto(film);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto dislikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.deleteLikeFromFilmInStorage(id, userId);
        return convertToFilmDto(film);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        List<Film> films = filmService.getPopularFilmLimitCountFromStorage(count);
        return films.stream()
                .map(this::convertToFilmDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public FilmDto postFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь публикует новый фильм: {}", film);

        // Конвертируем Film в параметры для сохранения
        Film filmToSave = new Film();
        filmToSave.setName(film.getName());
        filmToSave.setDescription(film.getDescription());
        filmToSave.setReleaseDate(film.getReleaseDate());
        filmToSave.setDuration(film.getDuration());
        filmToSave.setGenres(film.getGenres());
        filmToSave.setMpa(film.getMpa());

        Film createdFilm = filmService.addFilmInStorage(filmToSave);
        return convertToFilmDto(createdFilm);
    }

    @PutMapping
    public FilmDto putFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь редактирует фильм: {}", film);

        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Film filmToUpdate = new Film();
        filmToUpdate.setId(film.getId());
        filmToUpdate.setName(film.getName());
        filmToUpdate.setDescription(film.getDescription());
        filmToUpdate.setReleaseDate(film.getReleaseDate());
        filmToUpdate.setDuration(film.getDuration());
        filmToUpdate.setGenres(film.getGenres());
        filmToUpdate.setMpa(film.getMpa());

        Film updatedFilm = filmService.updateFilmInStorage(filmToUpdate);
        return convertToFilmDto(updatedFilm);
    }

    @GetMapping
    public Collection<FilmDto> getAllFilms() {
        log.info("Пользователь запросил все фильмы");
        Collection<Film> films = filmService.getAllFilmsFromStorage();
        return films.stream()
                .map(this::convertToFilmDto)
                .collect(Collectors.toList());
    }

    private FilmDto convertToFilmDto(Film film) {
        if (film == null) return null;

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getMpa() != null) {
            Integer mpaId = mapMpaToId(film.getMpa());
            MpaDto mpaDto = mpaService.getMpaById(mpaId);
            dto.setMpa(mpaDto);
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<GenreDto> genreDtos = new LinkedHashSet<>();
            for (GenreOfFilm genre : film.getGenres()) {
                Integer genreId = mapGenreToId(genre);
                GenreDto genreDto = genreService.getGenreById(genreId);
                genreDtos.add(genreDto);
            }
            dto.setGenres(genreDtos);
        }

        if (film.getIdOfUsersWhoLikedThisFilm() != null) {
            dto.setLikesCount(film.getIdOfUsersWhoLikedThisFilm().size());
        } else {
            dto.setLikesCount(0);
        }

        return dto;
    }

    private Integer mapMpaToId(MotionPictureAssociation mpa) {
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

    private Integer mapGenreToId(GenreOfFilm genre) {
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
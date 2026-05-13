package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
@Transactional
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmByIdFromStorage(id);
        return FilmMapper.toDto(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto likeFilm(@PathVariable Long id,
                            @PathVariable Long userId) {
        Film film = filmService.postLikeOnFilmInStorage(id, userId);
        return FilmMapper.toDto(film);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto dislikeFilm(@PathVariable Long id,
                               @PathVariable Long userId) {
        Film film = filmService.deleteLikeFromFilmInStorage(id, userId);
        return FilmMapper.toDto(film);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        List<Film> films = filmService.getPopularFilmLimitCountFromStorage(count);
        return films.stream()
                .map(FilmMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public FilmDto postFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь публикует новый фильм " + film.toString());
        Film createdFilm = filmService.addFilmInStorage(film);
        return FilmMapper.toDto(createdFilm);
    }

    @PutMapping
    public FilmDto putFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь редактирует фильм " + film.toString());
        Film updatedFilm = filmService.updateFilmInStorage(film);
        return FilmMapper.toDto(updatedFilm);
    }

    @GetMapping
    public Collection<FilmDto> getAllFilms() {
        log.info("Пользователь запросил все фильмы");
        Collection<Film> films = filmService.getAllFilmsFromStorage();
        return films.stream()
                .map(FilmMapper::toDto)
                .collect(Collectors.toList());
    }
}
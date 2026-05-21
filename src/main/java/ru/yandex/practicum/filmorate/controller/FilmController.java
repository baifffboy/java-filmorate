package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.films.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.films.FilmResponse;
import ru.yandex.practicum.filmorate.dto.films.FilmUpdateRequest;
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
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public FilmResponse getFilmById(@PathVariable Long id) {
        log.info("Пользователь получает фильм по id = {}", id);
        return FilmMapper.toFilmResponse(filmService.getFilmByIdFromStorage(id));
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmResponse likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с id = {} ставит лайк фильму c id = {}", userId, id);
        return FilmMapper.toFilmResponse(filmService.postLikeOnFilmInStorage(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmResponse dislikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с id = {} удаляет лайк с фильма c id = {}", userId, id);
        return FilmMapper.toFilmResponse(filmService.deleteLikeFromFilmInStorage(id, userId));
    }

    @GetMapping("/popular")
    public List<FilmResponse> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        log.info("Пользователь запросил топ-{} популярных фильмов", count);
        List<Film> films = filmService.getPopularFilmLimitCountFromStorage(count);
        return films.stream()
                .map(FilmMapper::toFilmResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public FilmResponse postFilm(@Valid @RequestBody FilmCreateRequest request) throws ValidationException {
        log.info("Пользователь публикует новый фильм: {}", request);
        return FilmMapper.toFilmResponse(filmService.addFilmInStorage(request));
    }

    @PutMapping
    public FilmResponse putFilm(@Valid @RequestBody FilmUpdateRequest request) throws ValidationException {
        log.info("Пользователь редактирует фильм: {}", request);
        return FilmMapper.toFilmResponse(filmService.updateFilmInStorage(request));
    }

    @GetMapping
    public Collection<FilmResponse> getAllFilms() {
        log.info("Пользователь запросил все фильмы");
        Collection<Film> films = filmService.getAllFilmsFromStorage();
        return films.stream()
                .map(FilmMapper::toFilmResponse)
                .collect(Collectors.toList());
    }
}
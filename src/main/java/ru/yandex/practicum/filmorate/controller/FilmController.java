package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmByIdFromStorage(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film likeFilm(@PathVariable Long id,
                         @PathVariable Long userId) {
        return filmService.postLikeOnFilmInStorage(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film dislikeFilm(@PathVariable Long id,
                            @PathVariable Long userId) {
        return filmService.deleteLikeFromFilmInStorage(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilmLimitCountFromStorage(count);
    }

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь публикует новый фильм " + film.toString());
        return filmService.addFilmInStorage(film);
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь редактирует фильм " + film.toString());
        return filmService.updateFilmInStorage(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Пользователь запросил все фильмы");
        return filmService.getAllFilmsFromStorage();
    }
}

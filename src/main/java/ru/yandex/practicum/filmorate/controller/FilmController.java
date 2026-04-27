package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public FilmController(FilmService filmService, UserService userService) {
        this.userService = userService;
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmByIdFromStorage(id);
        if (film == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film likeFilm(@PathVariable Long id,
                         @PathVariable Long userId) {
        if (!filmService.containsKey(id) || !userService.containsKey(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        filmService.getFilmByIdFromStorage(id).getIdOfUsersWhoLikedThisFilm().add(userId);
        return filmService.getFilmByIdFromStorage(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film dislikeFilm(@PathVariable Long id,
                            @PathVariable Long userId) {
        if (!filmService.containsKey(id) || !filmService.getFilmByIdFromStorage(id).getIdOfUsersWhoLikedThisFilm().contains(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return filmService.deleteLikeFromFilmInStorage(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilmLimitCountFromStorage(count);
    }

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь публикует новый фильм " + film.toString());
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        Film postFilm = filmService.addFilmInStorage(film.getId(), film);
        log.info("Фильм создан с id={}", film.getId());
        return postFilm;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь редактирует фильм " + film.toString());
        if (film.getId() == null) {
            log.error("Ошибка валидации порядкового номера(id) фильма");
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmService.containsKey(film.getId())) {
            log.error("Ошибка - фильм не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост с id = " + film.getId() + " не найден");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        return filmService.updateFilmInStorage(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Пользователь запросил все фильмы");
        return filmService.getAllFilmsFromStorage();
    }

    private long getNextId() {
        return filmService.getNextIdFromStorage();
    }
}

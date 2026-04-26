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
        if (filmService.containsKey(id))
            filmService.getFilmByIdFromStorage(id).getIdOfUsersWhoLikedThisFilm().add(userId);
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return filmService.getFilmByIdFromStorage(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film dislikeFilm(@PathVariable Long id,
                            @PathVariable Long userId) {
        if (filmService.containsKey(id) && filmService.getFilmByIdFromStorage(id).getIdOfUsersWhoLikedThisFilm().contains(userId))
            filmService.deleteFilmInStorage(userId);
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return filmService.getFilmByIdFromStorage(id);
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
        filmService.addFilmInStorage(film.getId(), film);
        log.info("Фильм создан с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь редактирует фильм " + film.toString());
        if (film.getId() == null) {
            log.error("Ошибка валидации порядкового номера(id) фильма");
            throw new ValidationException("Id должен быть указан");
        }
        Film oldFilm;
        if (filmService.containsKey(film.getId())) {
            if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
            }
            oldFilm = filmService.getFilmByIdFromStorage(film.getId());
            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());
        } else {
            log.error("Ошибка - фильм не найден");
            throw new ValidationException("Пост с id = " + film.getId() + " не найден");
        }
        return oldFilm;
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

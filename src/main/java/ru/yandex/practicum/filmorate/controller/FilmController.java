package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь публикует новый фильм " + film.toString());
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
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
        if (films.containsKey(film.getId())) {
            if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
            }
            oldFilm = films.get(film.getId());
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
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь публикует новый фильм " + film.toString());
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации названия фильма");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.error("Ошибка валидации описания фильма");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        LocalDate date = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(date)) {
            log.error("Ошибка валидации релиза фильма");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации продолжительности фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Пользователь редактирует фильм " + film.toString());
        if (film.getId() == null) {
            log.error("Ошибка валидации порядкового номера(id) фильма");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());
            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());
        } else {
            log.error("Ошибка - фильм не найден");
            throw new ValidationException("Пост с id = " + film.getId() + " не найден");
        }
        return film;
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

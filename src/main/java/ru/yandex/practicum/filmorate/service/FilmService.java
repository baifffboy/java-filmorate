package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.userService = userService;
        this.filmStorage = filmStorage;
    }

    public Film getFilmByIdFromStorage(Long id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return film;
    }

    public boolean containsKey(Long id) {
        return filmStorage.containsKey(id);
    }

    public List<Film> getPopularFilmLimitCountFromStorage(int count) {
        return filmStorage.getPopularFilmLimitCount(count);
    }

    public Film addFilmInStorage(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        film.setId(filmStorage.getNextId());
        return filmStorage.addFilm(film);
    }

    public Film deleteLikeFromFilmInStorage(Long id, Long whoIsDeleted) {
        if (!userService.containsKey(whoIsDeleted) || !this.getFilmByIdFromStorage(id).getIdOfUsersWhoLikedThisFilm().contains(whoIsDeleted))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return filmStorage.deleteLikeFromFilm(id, whoIsDeleted);
    }

    public Film postLikeOnFilmInStorage(Long id, Long userId) {
        if (!this.containsKey(id) || !userService.containsKey(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        this.getFilmByIdFromStorage(id).getIdOfUsersWhoLikedThisFilm().add(userId);
        return this.getFilmByIdFromStorage(id);
    }

    public Collection<Film> getAllFilmsFromStorage() {
        return filmStorage.getAllValues();
    }

    public Film updateFilmInStorage(Film film) {
        if (film.getId() == null) {
            log.error("Ошибка валидации порядкового номера(id) фильма");
            throw new ValidationException("Id должен быть указан");
        }
        if (!this.containsKey(film.getId())) {
            log.error("Ошибка - фильм не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост с id = " + film.getId() + " не найден");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        return filmStorage.updateFilm(film);
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserService userService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("jdbcFilmStorage") FilmRepository filmRepository, UserService userService) {
        this.filmRepository = filmRepository;
        this.userService = userService;
    }

    public Film getFilmByIdFromStorage(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        List<Long> likes = filmRepository.findLikes(id);
        film.getIdOfUsersWhoLikedThisFilm().clear();
        film.getIdOfUsersWhoLikedThisFilm().addAll(likes);

        return film;
    }

    public Collection<Film> getAllFilmsFromStorage() {
        return filmRepository.findAll();
    }

    public Film addFilmInStorage(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        return filmRepository.save(film);
    }

    public Film updateFilmInStorage(Film film) {
        if (film.getId() == null) {
            log.error("Ошибка валидации порядкового номера(id) фильма");
            throw new ValidationException("Id должен быть указан");
        }

        filmRepository.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        return filmRepository.update(film);
    }

    public Film postLikeOnFilmInStorage(Long id, Long userId) {
        Film film = getFilmByIdFromStorage(id);
        User user = userService.getUserByIdFromStorage(userId);

        if (!film.getIdOfUsersWhoLikedThisFilm().contains(userId)) {
            filmRepository.addLike(id, userId);
            film.getIdOfUsersWhoLikedThisFilm().add(userId);
        }
        return getFilmByIdFromStorage(id);
    }

    public Film deleteLikeFromFilmInStorage(Long id, Long userId) {
        Film film = getFilmByIdFromStorage(id);
        userService.getUserByIdFromStorage(userId);

        if (!film.getIdOfUsersWhoLikedThisFilm().contains(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не ставил лайк фильму " + id);
        }

        filmRepository.deleteLike(id, userId);
        film.getIdOfUsersWhoLikedThisFilm().remove(userId);
        return getFilmByIdFromStorage(id);
    }

    public List<Film> getPopularFilmLimitCountFromStorage(int count) {
        return filmRepository.findPopular(count);
    }
}
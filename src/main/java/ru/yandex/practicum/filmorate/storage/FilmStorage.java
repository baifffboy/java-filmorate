package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    void loadGenresForFilm(Film film);

    List<Film> findAll();

    Optional<Film> findById(long filmId);

    Film save(Film film);

    Film update(Film film);

    void updateGenres(Film film);

    boolean delete(long id);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    List<Long> findLikes(long filmId);

    List<Film> findPopular(int limit);
}
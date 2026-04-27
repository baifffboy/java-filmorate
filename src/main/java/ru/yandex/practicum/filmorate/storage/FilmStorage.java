package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film getFilmById(Long id);

    boolean containsKey(Long id);

    List<Film> getPopularFilmLimitCount(int count);

    Film addFilm(Long id, Film film);

    Film deleteLikeFromFilm(Long id, Long whoIsDeleted);

    Collection<Film> getAllValues();

    long getNextId();

    Film updateFilm(Film film);
}

package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film getFilmById(Long id);

    boolean containsKey(Long id);

    List<Film> getPopularFilmLimitCount(int count);

    Long addFilm(Long id, Film film);

    Long deleteFilm(Long id);

    Collection<Film> getAllValues();

    long getNextId();
}

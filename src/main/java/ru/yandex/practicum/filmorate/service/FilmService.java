package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;

@Service
public class FilmService {

    private FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film getFilmByIdFromStorage(Long id) {
        return filmStorage.getFilmById(id);
    }

    public boolean containsKey(Long id) {
        return filmStorage.containsKey(id);
    }

    public List<Film> getPopularFilmLimitCountFromStorage(int count) {
        return filmStorage.getPopularFilmLimitCount(count);
    }

    public Long addFilmInStorage(Long id, Film film) {
        return filmStorage.addFilm(id, film);
    }

    public Long deleteFilmInStorage(Long id) {
        return filmStorage.deleteFilm(id);
    }

    public Collection<Film> getAllFilmsFromStorage() {
        return filmStorage.getAllValues();
    }

    public long getNextIdFromStorage() {
        return filmStorage.getNextId();
    }
}

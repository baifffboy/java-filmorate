package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public boolean containsKey(Long id) {
        return films.containsKey(id);
    }

    @Override
    public List<Film> getPopularFilmLimitCount(int count) {
        return films.values().stream()
                .sorted((map1, map2) -> Integer.compare(
                        map2.getIdOfUsersWhoLikedThisFilm().size(),
                        map1.getIdOfUsersWhoLikedThisFilm().size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film addFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Фильм создан с id={}", film.getId());
        return films.get(film.getId());
    }

    @Override
    public Collection<Film> getAllValues() {
        return films.values();
    }

    @Override
    public long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Film deleteLikeFromFilm(Long id, Long whoIsDeleted) {
        films.get(id).getIdOfUsersWhoLikedThisFilm().remove(whoIsDeleted);
        return films.get(id);
    }

    @Override
    public Film updateFilm(Film film) {
        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) return null;
        Set<Long> existingIdOfUsersWhoLikedThisFilm = existingFilm.getIdOfUsersWhoLikedThisFilm();
        film.setIdOfUsersWhoLikedThisFilm(existingIdOfUsersWhoLikedThisFilm);
        films.put(film.getId(), film);
        return film;
    }
}

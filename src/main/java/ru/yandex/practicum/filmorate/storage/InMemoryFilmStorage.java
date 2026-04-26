package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
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
    public Long addFilm(Long id, Film film) {
        films.put(id, film);
        return id;
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
    public Long deleteFilm(Long id) {
        films.remove(id);
        return id;
    }
}

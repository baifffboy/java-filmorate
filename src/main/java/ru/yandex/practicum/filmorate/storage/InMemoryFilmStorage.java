package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();
    private final Map<Long, Set<GenreOfFilm>> filmGenres = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Film> findById(long filmId) {
        Film film = films.get(filmId);
        if (film != null) {
            loadGenresForFilm(film);
            film.getIdOfUsersWhoLikedThisFilm().clear();
            film.getIdOfUsersWhoLikedThisFilm().addAll(findLikes(filmId));
        }
        return Optional.ofNullable(film);
    }

    @Override
    public List<Film> findPopular(int limit) {
        return films.values().stream()
                .sorted((film1, film2) -> Integer.compare(
                        findLikes(film2.getId()).size(),
                        findLikes(film1.getId()).size()
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Film save(Film film) {
        long id = idGenerator.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            filmGenres.put(id, new LinkedHashSet<>(film.getGenres()));
        }
        updateGenres(film);
        log.info("Фильм создан с id={}", film.getId());
        return findById(id).orElse(film);
    }

    @Override
    public List<Film> findAll() {
        List<Film> filmList = new ArrayList<>();
        for (Film film : films.values()) {
            loadGenresForFilm(film);
            film.getIdOfUsersWhoLikedThisFilm().clear();
            film.getIdOfUsersWhoLikedThisFilm().addAll(findLikes(film.getId()));
            filmList.add(film);
        }
        return filmList;
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        Set<Long> likes = filmLikes.getOrDefault(filmId, new HashSet<>());
        if (likes.remove(userId)) {
            filmLikes.put(filmId, likes);
            Film film = films.get(filmId);
            if (film != null) {
                film.getIdOfUsersWhoLikedThisFilm().remove(userId);
            }
            log.info("Лайк от пользователя {} к фильму {} удален", userId, filmId);
        }
    }

    @Override
    public Film update(Film film) {
        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) return null;

        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setReleaseDate(film.getReleaseDate());
        existingFilm.setDuration(film.getDuration());
        existingFilm.setMpa(film.getMpa());

        updateGenres(film);

        log.info("Фильм с id={} обновлен", film.getId());
        return findById(film.getId()).orElse(existingFilm);
    }

    @Override
    public void loadGenresForFilm(Film film) {
        if (film == null || film.getId() == null) return;
        Set<GenreOfFilm> genres = filmGenres.getOrDefault(film.getId(), new LinkedHashSet<>());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    @Override
    public void updateGenres(Film film) {
        if (film == null || film.getId() == null) return;
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            filmGenres.put(film.getId(), new LinkedHashSet<>(film.getGenres()));
        } else {
            filmGenres.remove(film.getId());
        }
    }

    @Override
    public boolean delete(long id) {
        Film removed = films.remove(id);
        if (removed != null) {
            filmLikes.remove(id);
            filmGenres.remove(id);
            log.info("Фильм с id={} удален", id);
            return true;
        }
        return false;
    }

    @Override
    public void addLike(long filmId, long userId) {
        Set<Long> likes = filmLikes.getOrDefault(filmId, new HashSet<>());
        if (likes.add(userId)) {
            filmLikes.put(filmId, likes);
            Film film = films.get(filmId);
            if (film != null) {
                film.getIdOfUsersWhoLikedThisFilm().add(userId);
            }
            log.info("Лайк от пользователя {} к фильму {} добавлен", userId, filmId);
        }
    }

    @Override
    public List<Long> findLikes(long filmId) {
        return new ArrayList<>(filmLikes.getOrDefault(filmId, new HashSet<>()));
    }
}
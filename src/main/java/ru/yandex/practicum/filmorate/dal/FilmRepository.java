package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MapMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository("jdbcFilmStorage")
public class FilmRepository extends BaseRepository<Film> {

    private static final String FIND_ALL_QUERY =
            "SELECT f.* FROM films f";

    private static final String FIND_BY_ID_QUERY =
            "SELECT f.* FROM films f WHERE f.id = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";

    private static final String FIND_POPULAR_QUERY =
            "SELECT f.*, COUNT(l.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";

    private static final String ADD_FILM_GENRE_QUERY = "INSERT INTO films_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM films_genres WHERE film_id = ?";
    private static final String FIND_FILM_GENRES_QUERY =
            "SELECT g.name FROM genres g " +
                    "JOIN films_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.id";

    private final JdbcTemplate jdbc;

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    @Override
    protected List<Film> findMany(String query, Object... params) {
        List<Film> films = super.findMany(query, params);
        for (Film film : films) {
            loadGenresForFilm(film);
        }
        return films;
    }

    @Override
    protected Optional<Film> findOne(String query, Object... params) {
        Optional<Film> filmOpt = super.findOne(query, params);
        filmOpt.ifPresent(this::loadGenresForFilm);
        return filmOpt;
    }

    private void loadGenresForFilm(Film film) {
        List<String> genreNames = jdbc.queryForList(FIND_FILM_GENRES_QUERY, String.class, film.getId());
        Set<GenreOfFilm> genres = genreNames.stream()
                .map(GenreMapper::mapStringToGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(genres);
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Film> findById(long filmId) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, filmId);
        filmOpt.ifPresent(film -> {
            Integer mpaId = null;
            mpaId = jdbc.queryForObject("SELECT mpa_id FROM films WHERE id = ?", Integer.class, filmId);
            if (mpaId != null) {
                film.setMpa(MapMapper.mapIdToMpa(mpaId));
            }
        });
        return filmOpt;
    }

    public Film save(Film film) {
        Integer mpaId = MapMapper.mapMpaToId(film.getMpa());
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId
        );
        film.setId(id);
        updateGenres(film);
        if (film.getMpa() != null) {
            film.setMpa(MapMapper.mapIdToMpa(mpaId));
        }
        return film;
    }

    public Film update(Film film) {
        Integer mpaId = MapMapper.mapMpaToId(film.getMpa());
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId()
        );
        updateGenres(film);
        if (film.getMpa() != null) {
            film.setMpa(MapMapper.mapIdToMpa(mpaId));
        }
        return film;
    }

    private void updateGenres(Film film) {
        jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (GenreOfFilm genre : film.getGenres()) {
                Integer genreId = GenreMapper.mapGenreToId(genre);
                if (genreId != null) {
                    jdbc.update(ADD_FILM_GENRE_QUERY, film.getId(), genreId);
                }
            }
        }
    }

    public boolean delete(long id) {
        return delete(DELETE_QUERY, id);
    }

    public void addLike(long filmId, long userId) {
        String checkQuery = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkQuery, Integer.class, filmId, userId);
        if (count == 0) {
            update(ADD_LIKE_QUERY, filmId, userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        String checkQuery = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkQuery, Integer.class, filmId, userId);
        if (count == 0) {
            throw new NotFoundException("Лайк от пользователя " + userId + " к фильму " + filmId + " не найден");
        }
        update(DELETE_LIKE_QUERY, filmId, userId);
    }

    public List<Long> findLikes(long filmId) {
        return jdbc.queryForList(FIND_LIKES_QUERY, Long.class, filmId);
    }

    public List<Film> findPopular(int limit) {
        return findMany(FIND_POPULAR_QUERY, limit);
    }
}
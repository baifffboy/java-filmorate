package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository extends BaseRepository<GenreResponse> {

    private static final String FIND_ALL_QUERY = "SELECT id, name FROM genres ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM genres WHERE id = ?";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<GenreResponse> mapper) {
        super(jdbc, mapper);
    }

    public List<GenreResponse> getAllGenres() {
        return findMany(FIND_ALL_QUERY);
    }

    public GenreResponse getGenreById(int id) {
        Optional<GenreResponse> result = findOne(FIND_BY_ID_QUERY, id);
        return result.orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
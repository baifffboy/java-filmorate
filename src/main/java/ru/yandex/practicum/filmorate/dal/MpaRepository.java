package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRepository extends BaseRepository<MpaResponse> {

    private static final String FIND_ALL_QUERY = "SELECT id, name FROM mpa ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM mpa WHERE id = ?";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MpaResponse> mapper) {
        super(jdbc, mapper);
    }

    public List<MpaResponse> getAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    public MpaResponse getMpaById(int id) {
        Optional<MpaResponse> result = findOne(FIND_BY_ID_QUERY, id);
        return result.orElseThrow(() -> new NotFoundException("Рейтинг MPA с id = " + id + " не найден"));
    }
}
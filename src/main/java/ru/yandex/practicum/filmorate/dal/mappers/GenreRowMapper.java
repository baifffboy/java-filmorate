package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreRowMapper implements RowMapper<GenreResponse> {

    @Override
    public GenreResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new GenreResponse(rs.getInt("id"), rs.getString("name"));
    }
}

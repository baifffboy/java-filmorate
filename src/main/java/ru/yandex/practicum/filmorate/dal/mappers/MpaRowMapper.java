package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<MpaResponse> {

    @Override
    public MpaResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MpaResponse(rs.getInt("id"), rs.getString("name"));
    }
}

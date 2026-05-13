package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreRowMapper implements RowMapper<GenreOfFilm> {

    @Override
    public GenreOfFilm mapRow(ResultSet rs, int rowNum) throws SQLException {
        String genreName = rs.getString("name");
        if (genreName == null) {
            return null;
        }

        switch (genreName) {
            case "Комедия":
                return GenreOfFilm.COMEDY;
            case "Драма":
                return GenreOfFilm.DRAMA;
            case "Мультфильм":
                return GenreOfFilm.CARTOON;
            case "Триллер":
                return GenreOfFilm.THRILLER;
            case "Документальный":
                return GenreOfFilm.DOCUMENTARY;
            case "Боевик":
                return GenreOfFilm.ACTION;
            default:
                return null;
        }
    }

    public static String toRussianName(GenreOfFilm genre) {
        if (genre == null) return null;
        switch (genre) {
            case COMEDY:
                return "Комедия";
            case DRAMA:
                return "Драма";
            case CARTOON:
                return "Мультфильм";
            case THRILLER:
                return "Триллер";
            case DOCUMENTARY:
                return "Документальный";
            case ACTION:
                return "Боевик";
            default:
                return null;
        }
    }
}
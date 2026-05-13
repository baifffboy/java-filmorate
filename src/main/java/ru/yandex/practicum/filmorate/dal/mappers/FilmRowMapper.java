package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        film.setReleaseDate(releaseDate);
        film.setDuration(rs.getInt("duration"));

        // Set MPA through id
        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            switch (mpaId) {
                case 1:
                    film.setMpa(MotionPictureAssociation.G);
                    break;
                case 2:
                    film.setMpa(MotionPictureAssociation.PG);
                    break;
                case 3:
                    film.setMpa(MotionPictureAssociation.PG13);
                    break;
                case 4:
                    film.setMpa(MotionPictureAssociation.R);
                    break;
                case 5:
                    film.setMpa(MotionPictureAssociation.NC17);
                    break;
                default:
                    film.setMpa(null);
            }
        }

        return film;
    }
}
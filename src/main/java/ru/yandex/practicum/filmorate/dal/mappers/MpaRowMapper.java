package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<MotionPictureAssociation> {

    @Override
    public MotionPictureAssociation mapRow(ResultSet rs, int rowNum) throws SQLException {
        String mpaName = rs.getString("name");
        if (mpaName == null) {
            return null;
        }

        switch (mpaName) {
            case "G":
                return MotionPictureAssociation.G;
            case "PG":
                return MotionPictureAssociation.PG;
            case "PG-13":
                return MotionPictureAssociation.PG13;
            case "R":
                return MotionPictureAssociation.R;
            case "NC-17":
                return MotionPictureAssociation.NC17;
            default:
                return null;
        }
    }
}
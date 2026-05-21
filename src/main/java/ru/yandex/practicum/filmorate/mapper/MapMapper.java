package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

public class MapMapper {
    public static Integer mapMpaToId(MotionPictureAssociation mpa) {
        if (mpa == null) return null;
        switch (mpa) {
            case G:
                return 1;
            case PG:
                return 2;
            case PG13:
                return 3;
            case R:
                return 4;
            case NC17:
                return 5;
            default:
                throw new NotFoundException("Данной категории нет в списке!");
        }
    }

    public static MotionPictureAssociation mapIdToMpa(Integer id) {
        switch (id) {
            case 1:
                return MotionPictureAssociation.G;
            case 2:
                return MotionPictureAssociation.PG;
            case 3:
                return MotionPictureAssociation.PG13;
            case 4:
                return MotionPictureAssociation.R;
            case 5:
                return MotionPictureAssociation.NC17;
            default:
                throw new NotFoundException("Данной категории нет в списке!");
        }
    }

    public static MpaResponse mapMpatoMpaResponse(Integer mpaId) {
        return new MpaResponse(mpaId, getMpaByIdtoString(mpaId));
    }

    public static String getMpaByIdtoString(int id) {
        switch (id) {
            case 1:
                return "G";
            case 2:
                return "PG";
            case 3:
                return "PG-13";
            case 4:
                return "R";
            case 5:
                return "NC-17";
            default:
                throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
        }
    }
}

package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Arrays;
import java.util.List;

@Service
public class MpaService {

    public List<MpaResponse> getAllMpa() {
        return Arrays.asList(
                new MpaResponse(1, "G"),
                new MpaResponse(2, "PG"),
                new MpaResponse(3, "PG-13"),
                new MpaResponse(4, "R"),
                new MpaResponse(5, "NC-17")
        );
    }

    public MpaResponse getMpaById(int id) {
        switch (id) {
            case 1:
                return new MpaResponse(1, "G");
            case 2:
                return new MpaResponse(2, "PG");
            case 3:
                return new MpaResponse(3, "PG-13");
            case 4:
                return new MpaResponse(4, "R");
            case 5:
                return new MpaResponse(5, "NC-17");
            default:
                throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
        }
    }
}

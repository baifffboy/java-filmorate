package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Arrays;
import java.util.List;

@Service
public class MpaService {

    public List<MpaDto> getAllMpa() {
        return Arrays.asList(
                new MpaDto(1, "G"),
                new MpaDto(2, "PG"),
                new MpaDto(3, "PG-13"),
                new MpaDto(4, "R"),
                new MpaDto(5, "NC-17")
        );
    }

    public MpaDto getMpaById(int id) {
        switch (id) {
            case 1:
                return new MpaDto(1, "G");
            case 2:
                return new MpaDto(2, "PG");
            case 3:
                return new MpaDto(3, "PG-13");
            case 4:
                return new MpaDto(4, "R");
            case 5:
                return new MpaDto(5, "NC-17");
            default:
                throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
        }
    }
}

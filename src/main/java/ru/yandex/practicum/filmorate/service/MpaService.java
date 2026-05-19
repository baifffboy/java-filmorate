package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;

import java.util.List;

@Service
public class MpaService {

    private final MpaRepository mpaRepository;

    public MpaService(MpaRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    public List<MpaResponse> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    public MpaResponse getMpaById(int id) {
        return mpaRepository.getMpaById(id);
    }
}

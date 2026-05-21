package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<GenreResponse> getAllGenres() {
        return genreRepository.getAllGenres();
    }

    public GenreResponse getGenreById(int id) {
        return genreRepository.getGenreById(id);
    }
}
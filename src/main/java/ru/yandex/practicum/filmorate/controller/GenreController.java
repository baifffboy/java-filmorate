package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {

    private final GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<GenreResponse> getAllGenres() {
        log.info("Пользователь хочет получить все жанры");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public GenreResponse getGenreById(@PathVariable int id) {
        log.info("Пользователь хочет получить все жанр по id={}", id);
        return genreService.getGenreById(id);
    }
}
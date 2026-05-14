package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(FilmService filmService, MpaService mpaService, GenreService genreService) {
        this.filmService = filmService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmByIdFromStorage(id);
        return convertToFilmDto(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.postLikeOnFilmInStorage(id, userId);
        return convertToFilmDto(film);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto dislikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.deleteLikeFromFilmInStorage(id, userId);
        return convertToFilmDto(film);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        List<Film> films = filmService.getPopularFilmLimitCountFromStorage(count);
        return films.stream()
                .map(this::convertToFilmDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public FilmDto postFilm(@Valid @RequestBody FilmCreateRequest request) throws ValidationException {
        log.info("Пользователь публикует новый фильм: {}", request);

        // Валидация даты релиза
        if (request.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        // ВАЖНО: Валидация MPA - проверяем, существует ли такой id
        Integer mpaId = null;
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            mpaId = request.getMpa().getId();
            try {
                mpaService.getMpaById(mpaId);
            } catch (NotFoundException e) {
                throw new NotFoundException("Рейтинг MPA с id = " + mpaId + " не найден");
            }
        }

        // ВАЖНО: Валидация жанров - проверяем, существуют ли такие id
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            for (GenreRequest genreReq : request.getGenres()) {
                if (genreReq.getId() != null) {
                    try {
                        genreService.getGenreById(genreReq.getId());
                    } catch (NotFoundException e) {
                        throw new NotFoundException("Жанр с id = " + genreReq.getId() + " не найден");
                    }
                }
            }
        }

        // Конвертируем запрос в Film
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (mpaId != null) {
            film.setMpa(mapIdToMpa(mpaId));
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            Set<GenreOfFilm> genres = new LinkedHashSet<>();
            for (GenreRequest genreReq : request.getGenres()) {
                if (genreReq.getId() != null) {
                    GenreOfFilm genre = mapIdToGenre(genreReq.getId());
                    if (genre != null) {
                        genres.add(genre);
                    }
                }
            }
            film.setGenres(genres);
        }

        Film createdFilm = filmService.addFilmInStorage(film);
        return convertToFilmDto(createdFilm);
    }

    @PutMapping
    public FilmDto putFilm(@Valid @RequestBody FilmUpdateRequest request) throws ValidationException {
        log.info("Пользователь редактирует фильм: {}", request);

        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (request.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        // Валидация MPA
        Integer mpaId = null;
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            mpaId = request.getMpa().getId();
            try {
                mpaService.getMpaById(mpaId);
            } catch (NotFoundException e) {
                throw new NotFoundException("Рейтинг MPA с id = " + mpaId + " не найден");
            }
        }

        // Валидация жанров
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            for (GenreRequest genreReq : request.getGenres()) {
                if (genreReq.getId() != null) {
                    try {
                        genreService.getGenreById(genreReq.getId());
                    } catch (NotFoundException e) {
                        throw new NotFoundException("Жанр с id = " + genreReq.getId() + " не найден");
                    }
                }
            }
        }

        // Конвертируем запрос в Film
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (mpaId != null) {
            film.setMpa(mapIdToMpa(mpaId));
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            Set<GenreOfFilm> genres = new LinkedHashSet<>();
            for (GenreRequest genreReq : request.getGenres()) {
                if (genreReq.getId() != null) {
                    GenreOfFilm genre = mapIdToGenre(genreReq.getId());
                    if (genre != null) {
                        genres.add(genre);
                    }
                }
            }
            film.setGenres(genres);
        }

        Film updatedFilm = filmService.updateFilmInStorage(film);
        return convertToFilmDto(updatedFilm);
    }

    @GetMapping
    public Collection<FilmDto> getAllFilms() {
        log.info("Пользователь запросил все фильмы");
        Collection<Film> films = filmService.getAllFilmsFromStorage();
        return films.stream()
                .map(this::convertToFilmDto)
                .collect(Collectors.toList());
    }

    private FilmDto convertToFilmDto(Film film) {
        if (film == null) return null;

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        // Количество лайков
        if (film.getIdOfUsersWhoLikedThisFilm() != null) {
            dto.setLikesCount(film.getIdOfUsersWhoLikedThisFilm().size());
        } else {
            dto.setLikesCount(0);
        }

        // MPA
        if (film.getMpa() != null) {
            Integer mpaId = mapMpaToId(film.getMpa());
            MpaDto mpaDto = mpaService.getMpaById(mpaId);
            dto.setMpa(mpaDto);  // ← setMpa
        }

        // Жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<GenreDto> genreDtos = new LinkedHashSet<>();
            for (GenreOfFilm genre : film.getGenres()) {
                Integer genreId = mapGenreToId(genre);
                GenreDto genreDto = genreService.getGenreById(genreId);
                genreDtos.add(genreDto);
            }
            dto.setGenres(genreDtos);  // ← setGenres
        }

        return dto;
    }

    private Integer mapMpaToId(MotionPictureAssociation mpa) {
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
                return null;
        }
    }

    private String getMpaName(MotionPictureAssociation mpa) {
        if (mpa == null) return null;
        switch (mpa) {
            case G:
                return "G";
            case PG:
                return "PG";
            case PG13:
                return "PG-13";
            case R:
                return "R";
            case NC17:
                return "NC-17";
            default:
                return null;
        }
    }

    private Integer mapGenreToId(GenreOfFilm genre) {
        if (genre == null) return null;
        switch (genre) {
            case COMEDY:
                return 1;
            case DRAMA:
                return 2;
            case CARTOON:
                return 3;
            case THRILLER:
                return 4;
            case DOCUMENTARY:
                return 5;
            case ACTION:
                return 6;
            default:
                return null;
        }
    }

    private String getGenreName(GenreOfFilm genre) {
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

    private MotionPictureAssociation mapIdToMpa(int id) {
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
                return null;
        }
    }

    private GenreOfFilm mapIdToGenre(int id) {
        switch (id) {
            case 1:
                return GenreOfFilm.COMEDY;
            case 2:
                return GenreOfFilm.DRAMA;
            case 3:
                return GenreOfFilm.CARTOON;
            case 4:
                return GenreOfFilm.THRILLER;
            case 5:
                return GenreOfFilm.DOCUMENTARY;
            case 6:
                return GenreOfFilm.ACTION;
            default:
                return null;
        }
    }
}
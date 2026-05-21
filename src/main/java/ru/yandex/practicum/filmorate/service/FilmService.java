package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.films.FilmCreateRequest;
import ru.yandex.practicum.filmorate.dto.films.FilmUpdateRequest;
import ru.yandex.practicum.filmorate.dto.genres.GenreRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MapMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmRepository;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("jdbcFilmStorage") FilmStorage filmRepository, UserService userService,
                       MpaService mpaService, GenreService genreService) {
        this.filmRepository = filmRepository;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Film getFilmByIdFromStorage(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        List<Long> likes = filmRepository.findLikes(id);
        film.getIdOfUsersWhoLikedThisFilm().clear();
        film.getIdOfUsersWhoLikedThisFilm().addAll(likes);

        return film;
    }

    public Collection<Film> getAllFilmsFromStorage() {
        return filmRepository.findAll();
    }

    public Film addFilmInStorage(FilmCreateRequest request) {

        Integer mpaId = null;
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            mpaId = request.getMpa().getId();
            try {
                mpaService.getMpaById(mpaId);
            } catch (NotFoundException e) {
                throw new NotFoundException("Рейтинг MPA с id = " + mpaId + " не найден");
            }
        }

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

        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (mpaId != null) {
            film.setMpa(MapMapper.mapIdToMpa(mpaId));
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            Set<GenreOfFilm> genres = new LinkedHashSet<>();
            for (GenreRequest genreReq : request.getGenres()) {
                if (genreReq.getId() != null) {
                    GenreOfFilm genre = GenreMapper.mapIdToGenre(genreReq.getId());
                    if (genre != null) {
                        genres.add(genre);
                    }
                }
            }
            film.setGenres(genres);
        }

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        return filmRepository.save(film);
    }

    public Film updateFilmInStorage(FilmUpdateRequest request) {
        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Integer mpaId = null;
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            mpaId = request.getMpa().getId();
            try {
                mpaService.getMpaById(mpaId);
            } catch (NotFoundException e) {
                throw new NotFoundException("Рейтинг MPA с id = " + mpaId + " не найден");
            }
        }

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

        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (mpaId != null) {
            film.setMpa(MapMapper.mapIdToMpa(mpaId));
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            Set<GenreOfFilm> genres = new LinkedHashSet<>();
            for (GenreRequest genreReq : request.getGenres()) {
                if (genreReq.getId() != null) {
                    GenreOfFilm genre = GenreMapper.mapIdToGenre(genreReq.getId());
                    if (genre != null) {
                        genres.add(genre);
                    }
                }
            }
            film.setGenres(genres);
        }

        filmRepository.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} раньше допустимой", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        return filmRepository.update(film);
    }

    public Film postLikeOnFilmInStorage(Long id, Long userId) {
        Film film = getFilmByIdFromStorage(id);
        userService.getUserByIdFromStorage(userId);

        if (!film.getIdOfUsersWhoLikedThisFilm().contains(userId)) {
            filmRepository.addLike(id, userId);
            film.getIdOfUsersWhoLikedThisFilm().add(userId);
        }
        return getFilmByIdFromStorage(id);
    }

    public Film deleteLikeFromFilmInStorage(Long id, Long userId) {
        Film film = getFilmByIdFromStorage(id);

        if (!film.getIdOfUsersWhoLikedThisFilm().contains(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не ставил лайк фильму " + id);
        }

        filmRepository.deleteLike(id, userId);
        film.getIdOfUsersWhoLikedThisFilm().remove(userId);
        return getFilmByIdFromStorage(id);
    }

    public List<Film> getPopularFilmLimitCountFromStorage(int count) {
        return filmRepository.findPopular(count);
    }
}
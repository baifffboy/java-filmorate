package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FilmorateApplicationFilmsTests {

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GenreController genreController;

    @Autowired
    private MpaController mpaController;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM films_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    private Film createValidFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        Set<GenreOfFilm> genres = new LinkedHashSet<>();
        genres.add(GenreOfFilm.COMEDY);
        film.setGenres(genres);
        film.setMpa(MotionPictureAssociation.PG13);
        return film;
    }

    private User createValidUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    void postFilm_WithValidFilm_ShouldSucceed() throws ValidationException {
        Film film = createValidFilm("Valid Film", "Valid description", LocalDate.of(2024, 1, 1), 120);

        FilmDto result = filmController.postFilm(film);

        assertNotNull(result.getId());
        assertEquals("Valid Film", result.getName());
        assertEquals("Valid description", result.getDescription());
        assertEquals(LocalDate.of(2024, 1, 1), result.getReleaseDate());
        assertEquals(120, result.getDuration());
        assertNotNull(result.getLikesCount());
        assertEquals(0, result.getLikesCount());
    }

    @Test
    void postFilm_WithReleaseDateBefore18951228_ShouldThrowException() {
        Film film = createValidFilm("Valid Film", "Valid description", LocalDate.of(1895, 12, 27), 120);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.postFilm(film)
        );

        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void postFilm_WithReleaseDateExactly18951228_ShouldSucceed() throws ValidationException {
        Film film = createValidFilm("Valid Film", "Valid description", LocalDate.of(1895, 12, 28), 120);

        FilmDto result = filmController.postFilm(film);

        assertNotNull(result);
    }

    @Test
    void getFilmById_WithExistingId_ShouldReturnFilm() throws ValidationException {
        Film film = createValidFilm("Test Film", "Description", LocalDate.now(), 100);
        FilmDto created = filmController.postFilm(film);

        FilmDto result = filmController.getFilmById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("Test Film", result.getName());
    }

    @Test
    void getFilmById_WithNonExistentId_ShouldThrowNotFound() {
        assertThrows(NotFoundException.class,
                () -> filmController.getFilmById(999L));
    }

    @Test
    void putFilm_WithValidExistingFilm_ShouldUpdate() throws ValidationException {
        Film film = createValidFilm("Original Name", "Original description", LocalDate.of(2024, 1, 1), 120);
        FilmDto created = filmController.postFilm(film);

        Film updatedFilm = createValidFilm("Updated Name", "Updated description", LocalDate.of(2025, 1, 1), 150);
        updatedFilm.setId(created.getId());

        FilmDto result = filmController.putFilm(updatedFilm);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
        assertNotNull(result.getLikesCount());
    }

    @Test
    void putFilm_WithNonExistentId_ShouldThrowException() {
        Film film = createValidFilm("Test", "Test", LocalDate.now(), 100);
        film.setId(999L);

        assertThrows(NotFoundException.class,
                () -> filmController.putFilm(film));
    }

    @Test
    void putFilm_WithNullId_ShouldThrowException() {
        Film film = createValidFilm("Test", "Test", LocalDate.now(), 100);
        film.setId(null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.putFilm(film)
        );

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void getAllFilms_ShouldReturnAllFilms() throws ValidationException {
        Film film1 = createValidFilm("Film 1", "Desc 1", LocalDate.now(), 90);
        Film film2 = createValidFilm("Film 2", "Desc 2", LocalDate.now(), 120);
        filmController.postFilm(film1);
        filmController.postFilm(film2);

        Collection<FilmDto> allFilms = filmController.getAllFilms();

        assertEquals(2, allFilms.size());
    }

    @Test
    void getAllFilms_WhenNoFilms_ShouldReturnEmptyCollection() {
        Collection<FilmDto> allFilms = filmController.getAllFilms();

        assertTrue(allFilms.isEmpty());
    }

    @Test
    void likeFilm_ShouldAddUserToLikes() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(film);

        FilmDto result = filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result);
        assertEquals(1, result.getLikesCount());
    }

    @Test
    void likeFilm_WithNonExistentFilm_ShouldThrowException() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);

        assertThrows(NotFoundException.class,
                () -> filmController.likeFilm(999L, createdUser.getId()));
    }

    @Test
    void likeFilm_WithNonExistentUser_ShouldThrowException() throws ValidationException {
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(film);

        assertThrows(NotFoundException.class,
                () -> filmController.likeFilm(createdFilm.getId(), 999L));
    }

    @Test
    void likeFilm_MultipleTimes_ShouldNotDuplicate() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(film);

        filmController.likeFilm(createdFilm.getId(), createdUser.getId());
        filmController.likeFilm(createdFilm.getId(), createdUser.getId());
        FilmDto result = filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        assertEquals(1, result.getLikesCount());
    }

    @Test
    void dislikeFilm_ShouldRemoveUserFromLikes() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(film);
        filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        FilmDto result = filmController.dislikeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result);
        assertEquals(0, result.getLikesCount());
    }

    @Test
    void dislikeFilm_WhenUserDidNotLike_ShouldThrowException() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(film);

        assertThrows(NotFoundException.class,
                () -> filmController.dislikeFilm(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    void getPopularFilms_ShouldReturnSortedByLikes() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User user3 = createValidUser("user3@test.com", "user3", "User3", LocalDate.now().minusYears(20));
        UserDto createdUser1 = userController.postUser(user1);
        UserDto createdUser2 = userController.postUser(user2);
        UserDto createdUser3 = userController.postUser(user3);

        Film film1 = createValidFilm("Film 1", "Desc", LocalDate.now(), 100);
        Film film2 = createValidFilm("Film 2", "Desc", LocalDate.now(), 100);
        Film film3 = createValidFilm("Film 3", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm1 = filmController.postFilm(film1);
        FilmDto createdFilm2 = filmController.postFilm(film2);
        FilmDto createdFilm3 = filmController.postFilm(film3);

        filmController.likeFilm(createdFilm2.getId(), createdUser1.getId());
        filmController.likeFilm(createdFilm2.getId(), createdUser2.getId());
        filmController.likeFilm(createdFilm2.getId(), createdUser3.getId());

        filmController.likeFilm(createdFilm1.getId(), createdUser1.getId());

        var popularFilms = filmController.getPopularFilm(10);

        assertEquals(3, popularFilms.size());
        assertEquals(createdFilm2.getId(), popularFilms.get(0).getId());
        assertEquals(createdFilm1.getId(), popularFilms.get(1).getId());
        assertEquals(createdFilm3.getId(), popularFilms.get(2).getId());
    }

    @Test
    void getPopularFilms_WithLimitCount_ShouldReturnLimitedResults() throws ValidationException {
        for (int i = 0; i < 5; i++) {
            Film film = createValidFilm("Film " + i, "Desc", LocalDate.now(), 100);
            filmController.postFilm(film);
        }

        var popularFilms = filmController.getPopularFilm(3);

        assertEquals(3, popularFilms.size());
    }

    @Test
    void getPopularFilms_WhenNoFilms_ShouldReturnEmptyList() {
        var popularFilms = filmController.getPopularFilm(10);
        assertTrue(popularFilms.isEmpty());
    }

    @Test
    void getAllGenres_ShouldReturnSixGenres() {
        List<GenreDto> genres = genreController.getAllGenres();

        assertEquals(6, genres.size());
        assertEquals(1, genres.get(0).getId());
        assertEquals("Комедия", genres.get(0).getName());
        assertEquals(2, genres.get(1).getId());
        assertEquals("Драма", genres.get(1).getName());
        assertEquals(3, genres.get(2).getId());
        assertEquals("Мультфильм", genres.get(2).getName());
        assertEquals(4, genres.get(3).getId());
        assertEquals("Триллер", genres.get(3).getName());
        assertEquals(5, genres.get(4).getId());
        assertEquals("Документальный", genres.get(4).getName());
        assertEquals(6, genres.get(5).getId());
        assertEquals("Боевик", genres.get(5).getName());
    }

    @Test
    void getGenreById_WhenIdExists_ShouldReturnGenre() {
        GenreDto genre = genreController.getGenreById(1);

        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void getGenreById_WhenIdDoesNotExist_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> genreController.getGenreById(999));
    }

    @Test
    void getAllMpa_ShouldReturnFiveRatings() {
        List<MpaDto> mpaList = mpaController.getAllMpa();

        assertEquals(5, mpaList.size());
        assertEquals(1, mpaList.get(0).getId());
        assertEquals("G", mpaList.get(0).getName());
        assertEquals(2, mpaList.get(1).getId());
        assertEquals("PG", mpaList.get(1).getName());
        assertEquals(3, mpaList.get(2).getId());
        assertEquals("PG-13", mpaList.get(2).getName());
        assertEquals(4, mpaList.get(3).getId());
        assertEquals("R", mpaList.get(3).getName());
        assertEquals(5, mpaList.get(4).getId());
        assertEquals("NC-17", mpaList.get(4).getName());
    }

    @Test
    void getMpaById_WhenIdExists_ShouldReturnRating() {
        MpaDto mpa = mpaController.getMpaById(1);

        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
    }

    @Test
    void getMpaById_WhenIdDoesNotExist_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> mpaController.getMpaById(999));
    }
}
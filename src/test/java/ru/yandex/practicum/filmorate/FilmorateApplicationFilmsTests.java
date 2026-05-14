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
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    // ИСПРАВЛЕНО: создаём FilmCreateRequest вместо Film
    private FilmCreateRequest createValidFilmRequest(String name, String description, LocalDate releaseDate, int duration) {
        FilmCreateRequest request = new FilmCreateRequest();
        request.setName(name);
        request.setDescription(description);
        request.setReleaseDate(releaseDate);
        request.setDuration(duration);

        MpaRequest mpaRequest = new MpaRequest();
        mpaRequest.setId(3); // PG-13
        request.setMpa(mpaRequest);

        List<GenreRequest> genres = new ArrayList<>();
        GenreRequest genreRequest = new GenreRequest();
        genreRequest.setId(1); // Комедия
        genres.add(genreRequest);
        request.setGenres(genres);

        return request;
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
        FilmCreateRequest request = createValidFilmRequest("Valid Film", "Valid description", LocalDate.of(2024, 1, 1), 120);

        FilmDto result = filmController.postFilm(request);

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
        FilmCreateRequest request = createValidFilmRequest("Valid Film", "Valid description", LocalDate.of(1895, 12, 27), 120);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.postFilm(request)
        );

        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void postFilm_WithReleaseDateExactly18951228_ShouldSucceed() throws ValidationException {
        FilmCreateRequest request = createValidFilmRequest("Valid Film", "Valid description", LocalDate.of(1895, 12, 28), 120);

        FilmDto result = filmController.postFilm(request);

        assertNotNull(result);
    }

    @Test
    void getFilmById_WithExistingId_ShouldReturnFilm() throws ValidationException {
        FilmCreateRequest request = createValidFilmRequest("Test Film", "Description", LocalDate.now(), 100);
        FilmDto created = filmController.postFilm(request);

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
        // Создаём фильм
        FilmCreateRequest createRequest = createValidFilmRequest("Original Name", "Original description", LocalDate.of(2024, 1, 1), 120);
        FilmDto created = filmController.postFilm(createRequest);

        // Создаём запрос на обновление
        FilmUpdateRequest updateRequest = new FilmUpdateRequest();
        updateRequest.setId(created.getId());
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated description");
        updateRequest.setReleaseDate(LocalDate.of(2025, 1, 1));
        updateRequest.setDuration(150);

        MpaRequest mpaRequest = new MpaRequest();
        mpaRequest.setId(3);
        updateRequest.setMpa(mpaRequest);

        FilmDto result = filmController.putFilm(updateRequest);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
        assertNotNull(result.getLikesCount());
    }

    @Test
    void putFilm_WithNonExistentId_ShouldThrowException() {
        FilmUpdateRequest updateRequest = new FilmUpdateRequest();
        updateRequest.setId(999L);
        updateRequest.setName("Test");
        updateRequest.setDescription("Test");
        updateRequest.setReleaseDate(LocalDate.now());
        updateRequest.setDuration(100);

        MpaRequest mpaRequest = new MpaRequest();
        mpaRequest.setId(1);
        updateRequest.setMpa(mpaRequest);

        assertThrows(NotFoundException.class,
                () -> filmController.putFilm(updateRequest));
    }

    @Test
    void putFilm_WithNullId_ShouldThrowException() {
        FilmUpdateRequest updateRequest = new FilmUpdateRequest();
        updateRequest.setId(null);
        updateRequest.setName("Test");
        updateRequest.setDescription("Test");
        updateRequest.setReleaseDate(LocalDate.now());
        updateRequest.setDuration(100);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.putFilm(updateRequest)
        );

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void getAllFilms_ShouldReturnAllFilms() throws ValidationException {
        FilmCreateRequest request1 = createValidFilmRequest("Film 1", "Desc 1", LocalDate.now(), 90);
        FilmCreateRequest request2 = createValidFilmRequest("Film 2", "Desc 2", LocalDate.now(), 120);
        filmController.postFilm(request1);
        filmController.postFilm(request2);

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

        FilmCreateRequest request = createValidFilmRequest("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(request);

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
        FilmCreateRequest request = createValidFilmRequest("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(request);

        assertThrows(NotFoundException.class,
                () -> filmController.likeFilm(createdFilm.getId(), 999L));
    }

    @Test
    void likeFilm_MultipleTimes_ShouldNotDuplicate() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);

        FilmCreateRequest request = createValidFilmRequest("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(request);

        filmController.likeFilm(createdFilm.getId(), createdUser.getId());
        filmController.likeFilm(createdFilm.getId(), createdUser.getId());
        FilmDto result = filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        assertEquals(1, result.getLikesCount());
    }

    @Test
    void dislikeFilm_ShouldRemoveUserFromLikes() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);

        FilmCreateRequest request = createValidFilmRequest("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(request);

        filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        FilmDto result = filmController.dislikeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result);
        assertEquals(0, result.getLikesCount());
    }

    @Test
    void dislikeFilm_WhenUserDidNotLike_ShouldThrowException() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserDto createdUser = userController.postUser(user);

        FilmCreateRequest request = createValidFilmRequest("Film", "Desc", LocalDate.now(), 100);
        FilmDto createdFilm = filmController.postFilm(request);

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

        FilmCreateRequest request1 = createValidFilmRequest("Film 1", "Desc", LocalDate.now(), 100);
        FilmCreateRequest request2 = createValidFilmRequest("Film 2", "Desc", LocalDate.now(), 100);
        FilmCreateRequest request3 = createValidFilmRequest("Film 3", "Desc", LocalDate.now(), 100);

        FilmDto createdFilm1 = filmController.postFilm(request1);
        FilmDto createdFilm2 = filmController.postFilm(request2);
        FilmDto createdFilm3 = filmController.postFilm(request3);

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
            FilmCreateRequest request = createValidFilmRequest("Film " + i, "Desc", LocalDate.now(), 100);
            filmController.postFilm(request);
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
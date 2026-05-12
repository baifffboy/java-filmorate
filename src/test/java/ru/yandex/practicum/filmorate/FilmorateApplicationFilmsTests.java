package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationFilmsTests {

    private FilmController filmController;
    private UserController userController;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        filmController = new FilmController(new FilmService(filmStorage, userService));
        userController = new UserController(userService);
    }

    // ==================== ТЕСТЫ ДЛЯ POST FILM ====================

    @Test
    void postFilm_WithValidFilm_ShouldSucceed() throws ValidationException {
        Film film = createValidFilm("Valid Film", "Valid description", LocalDate.of(2024, 1, 1), 120);

        Film result = filmController.postFilm(film);

        assertNotNull(result.getId());
        assertEquals("Valid Film", result.getName());
        assertEquals("Valid description", result.getDescription());
        assertEquals(LocalDate.of(2024, 1, 1), result.getReleaseDate());
        assertEquals(120, result.getDuration());
        assertNotNull(result.getIdOfUsersWhoLikedThisFilm());
        assertTrue(result.getIdOfUsersWhoLikedThisFilm().isEmpty());
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

        Film result = filmController.postFilm(film);

        assertNotNull(result);
    }

    // ==================== ТЕСТЫ ДЛЯ GET FILM BY ID ====================

    @Test
    void getFilmById_WithExistingId_ShouldReturnFilm() throws ValidationException {
        Film film = createValidFilm("Test Film", "Description", LocalDate.now(), 100);
        Film created = filmController.postFilm(film);

        Film result = filmController.getFilmById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("Test Film", result.getName());
    }

    @Test
    void getFilmById_WithNonExistentId_ShouldThrowNotFound() {
        assertThrows(ResponseStatusException.class,
                () -> filmController.getFilmById(999L));
    }

    // ==================== ТЕСТЫ ДЛЯ PUT FILM ====================

    @Test
    void putFilm_WithValidExistingFilm_ShouldUpdate() throws ValidationException {
        Film film = createValidFilm("Original Name", "Original description", LocalDate.of(2024, 1, 1), 120);
        Film created = filmController.postFilm(film);

        Film updatedFilm = createValidFilm("Updated Name", "Updated description", LocalDate.of(2025, 1, 1), 150);
        updatedFilm.setId(created.getId());

        Film result = filmController.putFilm(updatedFilm);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
        assertNotNull(result.getIdOfUsersWhoLikedThisFilm());
    }

    @Test
    void putFilm_WithNonExistentId_ShouldThrowException() {
        Film film = createValidFilm("Test", "Test", LocalDate.now(), 100);
        film.setId(999L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> filmController.putFilm(film)
        );

        assertEquals("Пост с id = 999 не найден", exception.getReason());
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

    // ==================== ТЕСТЫ ДЛЯ GET ALL FILMS ====================

    @Test
    void getAllFilms_ShouldReturnAllFilms() throws ValidationException {
        Film film1 = createValidFilm("Film 1", "Desc 1", LocalDate.now(), 90);
        Film film2 = createValidFilm("Film 2", "Desc 2", LocalDate.now(), 120);
        filmController.postFilm(film1);
        filmController.postFilm(film2);

        Collection<Film> allFilms = filmController.getAllFilms();

        assertEquals(2, allFilms.size());
    }

    @Test
    void getAllFilms_WhenNoFilms_ShouldReturnEmptyCollection() {
        Collection<Film> allFilms = filmController.getAllFilms();

        assertTrue(allFilms.isEmpty());
    }

    // ==================== ТЕСТЫ ДЛЯ LIKE/DISLIKE FILM ====================

    @Test
    void likeFilm_ShouldAddUserToLikes() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        Film createdFilm = filmController.postFilm(film);

        Film result = filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        assertTrue(result.getIdOfUsersWhoLikedThisFilm().contains(createdUser.getId()));
        assertEquals(1, result.getIdOfUsersWhoLikedThisFilm().size());
    }

    @Test
    void likeFilm_WithNonExistentFilm_ShouldThrowException() {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);

        assertThrows(ResponseStatusException.class,
                () -> filmController.likeFilm(999L, createdUser.getId()));
    }

    @Test
    void likeFilm_WithNonExistentUser_ShouldThrowException() throws ValidationException {
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        Film createdFilm = filmController.postFilm(film);

        assertThrows(ResponseStatusException.class,
                () -> filmController.likeFilm(createdFilm.getId(), 999L));
    }

    @Test
    void dislikeFilm_ShouldRemoveUserFromLikes() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        Film createdFilm = filmController.postFilm(film);
        filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        Film result = filmController.dislikeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result);
        assertFalse(result.getIdOfUsersWhoLikedThisFilm().contains(createdUser.getId()));
        assertTrue(result.getIdOfUsersWhoLikedThisFilm().isEmpty());
    }

    @Test
    void dislikeFilm_WhenUserDidNotLike_ShouldThrowException() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        Film createdFilm = filmController.postFilm(film);

        assertThrows(ResponseStatusException.class,
                () -> filmController.dislikeFilm(createdFilm.getId(), createdUser.getId()));
    }

    // ==================== ТЕСТЫ ДЛЯ POPULAR FILMS ====================

    @Test
    void getPopularFilms_ShouldReturnSortedByLikes() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User user3 = createValidUser("user3@test.com", "user3", "User3", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);
        User createdUser3 = userController.postUser(user3);

        Film film1 = createValidFilm("Film 1", "Desc", LocalDate.now(), 100);
        Film film2 = createValidFilm("Film 2", "Desc", LocalDate.now(), 100);
        Film film3 = createValidFilm("Film 3", "Desc", LocalDate.now(), 100);
        Film createdFilm1 = filmController.postFilm(film1);
        Film createdFilm2 = filmController.postFilm(film2);
        Film createdFilm3 = filmController.postFilm(film3);

        // Film2 получает 3 лайка (самый популярный)
        filmController.likeFilm(createdFilm2.getId(), createdUser1.getId());
        filmController.likeFilm(createdFilm2.getId(), createdUser2.getId());
        filmController.likeFilm(createdFilm2.getId(), createdUser3.getId());

        // Film1 получает 1 лайк (средний)
        filmController.likeFilm(createdFilm1.getId(), createdUser1.getId());

        // Film3 получает 0 лайков (непопулярный)

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

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Film createValidFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
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
}
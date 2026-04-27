package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
class FilmorateApplicationTests {

    private FilmController filmController;
    private UserController userController;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmController = new FilmController(new FilmService(filmStorage));
        userController = new UserController(new UserService(userStorage));
    }

    // ==================== ТЕСТЫ ДЛЯ FILM CONTROLLER ====================

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
    void getFilmById_WithNonExistentId_ShouldReturnNull() {
        Film result = filmController.getFilmById(999L);

        assertNull(result);
    }

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
        // Проверяем, что список лайков сохранился
        assertNotNull(result.getIdOfUsersWhoLikedThisFilm());
    }

    @Test
    void putFilm_WithNonExistentId_ShouldThrowException() {
        Film film = createValidFilm("Test", "Test", LocalDate.now(), 100);
        film.setId(999L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.putFilm(film)
        );

        assertEquals("Пост с id = 999 не найден", exception.getMessage());
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

        Collection<Film> allFilms = filmController.getAllFilms();

        assertEquals(2, allFilms.size());
    }

    @Test
    void getAllFilms_WhenNoFilms_ShouldReturnEmptyCollection() {
        Collection<Film> allFilms = filmController.getAllFilms();

        assertTrue(allFilms.isEmpty());
    }

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
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> filmController.likeFilm(999L, 1L));
    }

    @Test
    void dislikeFilm_ShouldRemoveUserFromLikes() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        Film createdFilm = filmController.postFilm(film);
        filmController.likeFilm(createdFilm.getId(), createdUser.getId());

        Long result = filmController.dislikeFilm(createdFilm.getId(), createdUser.getId());

        assertNotNull(result);
        assertFalse(createdFilm.getIdOfUsersWhoLikedThisFilm().contains(createdUser.getId()));
        assertTrue(createdFilm.getIdOfUsersWhoLikedThisFilm().isEmpty());
    }

    @Test
    void dislikeFilm_WhenUserDidNotLike_ShouldThrowException() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);
        Film film = createValidFilm("Film", "Desc", LocalDate.now(), 100);
        Film createdFilm = filmController.postFilm(film);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> filmController.dislikeFilm(createdFilm.getId(), createdUser.getId()));
    }

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
        assertEquals(createdFilm2.getId(), popularFilms.get(0).getId()); // Самый популярный первый
        assertEquals(createdFilm1.getId(), popularFilms.get(1).getId()); // Средний второй
        assertEquals(createdFilm3.getId(), popularFilms.get(2).getId()); // Непопулярный третий
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

    // ==================== ТЕСТЫ ДЛЯ USER CONTROLLER ====================

    @Test
    void postUser_WithValidUser_ShouldSucceed() throws ValidationException {
        User user = createValidUser("user@example.com", "validLogin", "Valid Name", LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertNotNull(result.getId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("validLogin", result.getLogin());
        assertEquals("Valid Name", result.getName());
        assertNotNull(result.getFriends());
        assertTrue(result.getFriends().isEmpty());
    }

    @Test
    void postUser_WithBlankName_ShouldUseLoginAsName() throws ValidationException {
        User user = createValidUser("user@example.com", "validLogin", "", LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithNullName_ShouldUseLoginAsName() throws ValidationException {
        User user = createValidUser("user@example.com", "validLogin", null, LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithLoginContainingSpace_ShouldThrowException() {
        User user = createValidUser("user@example.com", "invalid login", "Valid Name", LocalDate.now().minusYears(20));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.postUser(user)
        );

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        User created = userController.postUser(user);

        User result = userController.getUserById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("user@test.com", result.getEmail());
    }

    @Test
    void getUserById_WithNonExistentId_ShouldReturnNull() {
        User result = userController.getUserById(999L);

        assertNull(result);
    }

    @Test
    void putUser_WithValidExistingUser_ShouldUpdate() throws ValidationException {
        User user = createValidUser("original@example.com", "originalLogin", "Original Name", LocalDate.now().minusYears(20));
        User created = userController.postUser(user);

        User updatedUser = createValidUser("updated@example.com", "updatedLogin", "Updated Name", LocalDate.now().minusYears(25));
        updatedUser.setId(created.getId());

        User result = userController.putUser(updatedUser);

        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
        assertEquals(LocalDate.now().minusYears(25), result.getBirthday());
        // Проверяем, что список друзей сохранился
        assertNotNull(result.getFriends());
    }

    @Test
    void putUser_WithBlankName_ShouldUseLoginAsName() throws ValidationException {
        User user = createValidUser("original@example.com", "originalLogin", "Original Name", LocalDate.now().minusYears(20));
        User created = userController.postUser(user);

        User updatedUser = createValidUser("updated@example.com", "updatedLogin", "", LocalDate.now().minusYears(25));
        updatedUser.setId(created.getId());

        User result = userController.putUser(updatedUser);

        assertEquals("updatedLogin", result.getName());
    }

    @Test
    void putUser_WithNonExistentId_ShouldThrowException() {
        User user = createValidUser("test@test.com", "test", "Test", LocalDate.now());
        user.setId(999L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.putUser(user)
        );

        assertEquals("Пост с id = 999 не найден", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws ValidationException {
        User user1 = createValidUser("user1@example.com", "user1", "User One", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@example.com", "user2", "User Two", LocalDate.now().minusYears(25));
        userController.postUser(user1);
        userController.postUser(user2);

        Collection<User> allUsers = userController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void addFriend_ShouldAddToBothUsersFriends() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);

        var result = userController.addFriend(createdUser1.getId(), createdUser2.getId());

        assertTrue(result.contains(createdUser1.getId()));
        assertTrue(result.contains(createdUser2.getId()));

        // Проверяем, что дружба взаимная
        assertTrue(createdUser1.getFriends().contains(createdUser2.getId()));
        assertTrue(createdUser2.getFriends().contains(createdUser1.getId()));
    }

    @Test
    void addFriend_WithNonExistentUser_ShouldThrowException() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userController.addFriend(createdUser1.getId(), 999L));
    }

    @Test
    void deleteFriend_ShouldRemoveFromBothUsers() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);
        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        var result = userController.deleteFriend(createdUser1.getId(), createdUser2.getId());

        assertNotNull(result);
        // Проверяем, что дружба удалена
        assertFalse(createdUser1.getFriends().contains(createdUser2.getId()));
        assertFalse(createdUser2.getFriends().contains(createdUser1.getId()));
    }

    @Test
    void getFriends_ShouldReturnUserFriends() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);
        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        var friends = userController.getFriends(createdUser1.getId());

        assertEquals(1, friends.size());
        assertTrue(friends.contains(createdUser2.getId()));
    }

    @Test
    void getFriends_WhenNoFriends_ShouldReturnEmptySet() throws ValidationException {
        User user = createValidUser("user@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User createdUser = userController.postUser(user);

        var friends = userController.getFriends(createdUser.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriends_ShouldReturnIntersection() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User common = createValidUser("common@test.com", "common", "Common", LocalDate.now().minusYears(20));

        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);
        User createdCommon = userController.postUser(common);

        userController.addFriend(createdUser1.getId(), createdCommon.getId());
        userController.addFriend(createdUser2.getId(), createdCommon.getId());

        var commonFriends = userController.commonFriend(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(createdCommon.getId()));
    }

    @Test
    void getCommonFriends_WhenNoCommon_ShouldReturnEmptySet() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User friend1 = createValidUser("friend1@test.com", "friend1", "Friend1", LocalDate.now().minusYears(20));
        User friend2 = createValidUser("friend2@test.com", "friend2", "Friend2", LocalDate.now().minusYears(20));

        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);
        User createdFriend1 = userController.postUser(friend1);
        User createdFriend2 = userController.postUser(friend2);

        userController.addFriend(createdUser1.getId(), createdFriend1.getId());
        userController.addFriend(createdUser2.getId(), createdFriend2.getId());

        var commonFriends = userController.commonFriend(createdUser1.getId(), createdUser2.getId());

        assertTrue(commonFriends.isEmpty());
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
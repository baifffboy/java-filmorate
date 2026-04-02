package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    // ==================== ТЕСТЫ ДЛЯ FILM CONTROLLER ====================

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2024, 1, 1));
        validFilm.setDuration(120);
    }

    // Тесты для POST /films
    @Test
    void postFilm_WithValidFilm_ShouldSucceed() throws ValidationException {
        Film result = filmController.postFilm(validFilm);

        assertNotNull(result.getId());
        assertEquals("Valid Film", result.getName());
        assertEquals("Valid description", result.getDescription());
        assertNotNull(result.getReleaseDate());
        assertEquals(120, result.getDuration());
    }

    @Test
    void postFilm_WithEmptyName_ShouldThrowException() {
        validFilm.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void postFilm_WithBlankName_ShouldThrowException() {
        validFilm.setName("   ");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void postFilm_WithNullName_ShouldThrowException() {
        validFilm.setName(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void postFilm_WithDescriptionLongerThan200_ShouldThrowException() {
        validFilm.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void postFilm_WithDescriptionExactly200_ShouldSucceed() throws ValidationException {
        validFilm.setDescription("a".repeat(200));

        Film result = filmController.postFilm(validFilm);

        assertNotNull(result);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void postFilm_WithReleaseDateBefore18951228_ShouldThrowException() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void postFilm_WithReleaseDateExactly18951228_ShouldSucceed() throws ValidationException {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));

        Film result = filmController.postFilm(validFilm);

        assertNotNull(result);
    }

    @Test
    void postFilm_WithReleaseDateAfter18951228_ShouldSucceed() throws ValidationException {
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));

        Film result = filmController.postFilm(validFilm);

        assertNotNull(result);
    }

    @Test
    void postFilm_WithNullReleaseDate_ShouldThrowException() {
        validFilm.setReleaseDate(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void postFilm_WithZeroDuration_ShouldThrowException() {
        validFilm.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void postFilm_WithNegativeDuration_ShouldThrowException() {
        validFilm.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));

        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void postFilm_WithNullDuration_ShouldThrowException() {
        // duration - примитив, не может быть null, но можно проверить значение по умолчанию (0)
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        // duration не устанавливаем, будет 0

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.postFilm(film));

        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    // Тесты для PUT /films
    @Test
    void putFilm_WithValidExistingFilm_ShouldUpdate() throws ValidationException {
        // Сначала создаем фильм
        Film created = filmController.postFilm(validFilm);

        // Создаем обновленную версию
        Film updatedFilm = new Film();
        updatedFilm.setId(created.getId());
        updatedFilm.setName("Updated Name");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2025, 1, 1));
        updatedFilm.setDuration(150);

        Film result = filmController.putFilm(updatedFilm);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    @Test
    void putFilm_WithNonExistentId_ShouldThrowException() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Test");
        film.setDescription("Test");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.putFilm(film));

        assertEquals("Пост с id = 999 не найден", exception.getMessage());
    }

    @Test
    void putFilm_WithNullId_ShouldThrowException() {
        Film film = new Film();
        film.setId(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.putFilm(film));

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    // Тесты для GET /films
    @Test
    void getAllFilms_ShouldReturnAllFilms() throws ValidationException {
        // Очищаем через создание нового контроллера для чистоты теста
        FilmController freshController = new FilmController();

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(90);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.now());
        film2.setDuration(120);

        freshController.postFilm(film1);
        freshController.postFilm(film2);

        var allFilms = freshController.getAllFilms();

        assertEquals(2, allFilms.size());
    }

    @Test
    void getAllFilms_WhenNoFilms_ShouldReturnEmptyCollection() {
        FilmController freshController = new FilmController();

        var allFilms = freshController.getAllFilms();

        assertTrue(allFilms.isEmpty());
    }

    // Тесты для ID generation
    @Test
    void filmIdGeneration_ShouldBeIncremental() throws ValidationException {
        FilmController freshController = new FilmController();

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(90);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.now());
        film2.setDuration(120);

        Film created1 = freshController.postFilm(film1);
        Film created2 = freshController.postFilm(film2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
    }

    // ==================== ТЕСТЫ ДЛЯ USER CONTROLLER ====================

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUpUserTests() {
        userController = new UserController();
        validUser = new User();
        validUser.setEmail("user@example.com");
        validUser.setLogin("validLogin");
        validUser.setName("Valid Name");
        validUser.setBirthday(LocalDate.now().minusYears(20));
    }

    // Тесты для POST /users
    @Test
    void postUser_WithValidUser_ShouldSucceed() throws ValidationException {
        User result = userController.postUser(validUser);

        assertNotNull(result.getId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("validLogin", result.getLogin());
        assertEquals("Valid Name", result.getName());
    }

    @Test
    void postUser_WithEmptyEmail_ShouldThrowException() {
        validUser.setEmail("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void postUser_WithBlankEmail_ShouldThrowException() {
        validUser.setEmail("   ");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void postUser_WithNullEmail_ShouldThrowException() {
        validUser.setEmail(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void postUser_WithEmailWithoutAtSymbol_ShouldThrowException() {
        validUser.setEmail("userexample.com");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void postUser_WithEmptyLogin_ShouldThrowException() {
        validUser.setLogin("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithBlankLogin_ShouldThrowException() {
        validUser.setLogin("   ");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithNullLogin_ShouldThrowException() {
        validUser.setLogin(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithLoginContainingSpace_ShouldThrowException() {
        validUser.setLogin("invalid login");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithBlankName_ShouldUseLoginAsName() throws ValidationException {
        validUser.setName("");

        User result = userController.postUser(validUser);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithNullName_ShouldUseLoginAsName() throws ValidationException {
        validUser.setName(null);

        User result = userController.postUser(validUser);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithValidName_ShouldKeepName() throws ValidationException {
        User result = userController.postUser(validUser);

        assertEquals("Valid Name", result.getName());
    }

    @Test
    void postUser_WithBirthdayInFuture_ShouldThrowException() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void postUser_WithBirthdayExactlyToday_ShouldSucceed() throws ValidationException {
        validUser.setBirthday(LocalDate.now());

        User result = userController.postUser(validUser);

        assertNotNull(result);
    }

    @Test
    void postUser_WithBirthdayInPast_ShouldSucceed() throws ValidationException {
        User result = userController.postUser(validUser);

        assertNotNull(result);
    }

    @Test
    void postUser_WithNullBirthday_ShouldThrowException() {
        validUser.setBirthday(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    // Тесты для PUT /users
    @Test
    void putUser_WithValidExistingUser_ShouldUpdate() throws ValidationException {
        // Сначала создаем пользователя
        User created = userController.postUser(validUser);

        // Создаем обновленную версию
        User updatedUser = new User();
        updatedUser.setId(created.getId());
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.now().minusYears(25));

        User result = userController.putUser(updatedUser);

        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void putUser_WithNonExistentId_ShouldThrowException() {
        User user = new User();
        user.setId(999L);
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.now());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.putUser(user));

        assertEquals("Пост с id = 999 не найден", exception.getMessage());
    }

    @Test
    void putUser_WithNullId_ShouldThrowException() {
        User user = new User();
        user.setId(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.putUser(user));

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    // Тесты для GET /users
    @Test
    void getAllUsers_ShouldReturnAllUsers() throws ValidationException {
        // Очищаем через создание нового контроллера для чистоты теста
        UserController freshController = new UserController();

        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.now().minusYears(20));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.now().minusYears(25));

        freshController.postUser(user1);
        freshController.postUser(user2);

        var allUsers = freshController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyCollection() {
        UserController freshController = new UserController();

        var allUsers = freshController.getAllUsers();

        assertTrue(allUsers.isEmpty());
    }

    // Тесты для ID generation
    @Test
    void userIdGeneration_ShouldBeIncremental() throws ValidationException {
        UserController freshController = new UserController();

        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.now().minusYears(20));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.now().minusYears(25));

        User created1 = freshController.postUser(user1);
        User created2 = freshController.postUser(user2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ ГРАНИЧНЫЕ ТЕСТЫ ====================

    @Test
    void postFilm_MultipleFilms_ShouldHaveUniqueIds() throws ValidationException {
        FilmController freshController = new FilmController();

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(90);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.now());
        film2.setDuration(120);

        Film created1 = freshController.postFilm(film1);
        Film created2 = freshController.postFilm(film2);

        assertNotEquals(created1.getId(), created2.getId());
    }

    @Test
    void postUser_MultipleUsers_ShouldHaveUniqueIds() throws ValidationException {
        UserController freshController = new UserController();

        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.now().minusYears(20));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.now().minusYears(25));

        User created1 = freshController.postUser(user1);
        User created2 = freshController.postUser(user2);

        assertNotEquals(created1.getId(), created2.getId());
    }

    @Test
    void postFilm_WithDescriptionBoundary200_ShouldSucceed() throws ValidationException {
        validFilm.setDescription("a".repeat(200));

        Film result = filmController.postFilm(validFilm);

        assertNotNull(result);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void postFilm_WithDescriptionBoundary201_ShouldThrowException() {
        validFilm.setDescription("a".repeat(201));

        assertThrows(ValidationException.class,
                () -> filmController.postFilm(validFilm));
    }

    @Test
    void postUser_WithLoginBoundary_SingleCharacter() throws ValidationException {
        validUser.setLogin("a");

        User result = userController.postUser(validUser);

        assertNotNull(result);
        assertEquals("a", result.getLogin());
    }

    @Test
    void postUser_WithEmailBoundary_MinimalValidEmail() throws ValidationException {
        validUser.setEmail("a@b.ru");

        User result = userController.postUser(validUser);

        assertNotNull(result);
        assertEquals("a@b.ru", result.getEmail());
    }

    @Test
    void postUser_WithWhitespaceInLogin_ShouldThrowException() {
        validUser.setLogin("login with spaces");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithLoginStartingWithSpace_ShouldThrowException() {
        validUser.setLogin(" login");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.postUser(validUser));

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }
}
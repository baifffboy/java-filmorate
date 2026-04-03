package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        userController = new UserController();
    }

    // ==================== ТЕСТЫ ДЛЯ FILM CONTROLLER ====================

    @Test
    void postFilm_WithValidFilm_ShouldSucceed() throws ValidationException {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film result = filmController.postFilm(film);

        assertNotNull(result.getId());
        assertEquals("Valid Film", result.getName());
        assertEquals("Valid description", result.getDescription());
        assertNotNull(result.getReleaseDate());
        assertEquals(120, result.getDuration());
    }

    @Test
    void postFilm_WithEmptyName_ShouldThrowException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("name").getDefaultMessage();
        assertEquals("Название не может быть пустым", message);
    }

    @Test
    void postFilm_WithBlankName_ShouldThrowException() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("name").getDefaultMessage();
        assertEquals("Название не может быть пустым", message);
    }

    @Test
    void postFilm_WithNullName_ShouldThrowException() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("name").getDefaultMessage();
        assertEquals("Название не может быть null", message);
    }

    @Test
    void postFilm_WithDescriptionLongerThan200_ShouldThrowException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("description").getDefaultMessage();
        assertEquals("Максимальная длина описания — 200 символов", message);
    }

    @Test
    void postFilm_WithDescriptionExactly200_ShouldSucceed() throws ValidationException {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film result = filmController.postFilm(film);

        assertNotNull(result);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void postFilm_WithReleaseDateBefore18951228_ShouldThrowException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.postFilm(film)
        );

        assertEquals("Дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void postFilm_WithReleaseDateExactly18951228_ShouldSucceed() throws ValidationException {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        Film result = filmController.postFilm(film);

        assertNotNull(result);
    }

    @Test
    void postFilm_WithReleaseDateAfter18951228_ShouldSucceed() throws ValidationException {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film result = filmController.postFilm(film);

        assertNotNull(result);
    }

    @Test
    void postFilm_WithNullReleaseDate_ShouldThrowException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(null);
        film.setDuration(120);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("releaseDate").getDefaultMessage();
        assertEquals("Дата релиза не может быть null", message);
    }

    @Test
    void postFilm_WithZeroDuration_ShouldThrowException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(0);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("duration").getDefaultMessage();
        assertEquals("Продолжительность должна быть положительным числом", message);
    }

    @Test
    void postFilm_WithNegativeDuration_ShouldThrowException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(-10);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("duration").getDefaultMessage();
        assertEquals("Продолжительность должна быть положительным числом", message);
    }

    @Test
    void postFilm_WithNullDuration_ShouldThrowException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(null);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film)
        );

        String message = exception.getBindingResult().getFieldError("duration").getDefaultMessage();
        assertEquals("Продолжительность не может быть null", message);
    }

    @Test
    void putFilm_WithValidExistingFilm_ShouldUpdate() throws ValidationException {
        // Создаем фильм
        Film film = new Film();
        film.setName("Original Name");
        film.setDescription("Original description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        Film created = filmController.postFilm(film);

        // Обновляем фильм
        Film updatedFilm = new Film();
        updatedFilm.setId(created.getId());
        updatedFilm.setName("Updated Name");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2025, 1, 1));
        updatedFilm.setDuration(150);

        Film result = filmController.putFilm(updatedFilm);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), result.getReleaseDate());
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

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.putFilm(film)
        );

        assertEquals("Пост с id = 999 не найден", exception.getMessage());
    }

    @Test
    void putFilm_WithNullId_ShouldThrowException() {
        Film film = new Film();
        film.setId(null);
        film.setName("Test");
        film.setDescription("Test");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.putFilm(film)
        );

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void getAllFilms_ShouldReturnAllFilms() throws ValidationException {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(90);
        filmController.postFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.now());
        film2.setDuration(120);
        filmController.postFilm(film2);

        var allFilms = filmController.getAllFilms();

        assertEquals(2, allFilms.size());
    }

    @Test
    void getAllFilms_WhenNoFilms_ShouldReturnEmptyCollection() {
        FilmController freshController = new FilmController();
        var allFilms = freshController.getAllFilms();
        assertTrue(allFilms.isEmpty());
    }

    @Test
    void filmIdGeneration_ShouldBeIncremental() throws ValidationException {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(90);
        Film created1 = filmController.postFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.now());
        film2.setDuration(120);
        Film created2 = filmController.postFilm(film2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
    }

    // ==================== ТЕСТЫ ДЛЯ USER CONTROLLER ====================

    @Test
    void postUser_WithValidUser_ShouldSucceed() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertNotNull(result.getId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("validLogin", result.getLogin());
        assertEquals("Valid Name", result.getName());
    }

    @Test
    void postUser_WithEmptyEmail_ShouldThrowException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("email").getDefaultMessage();
        assertEquals("Email не может быть пустым", message);
    }

    @Test
    void postUser_WithBlankEmail_ShouldThrowException() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("email").getDefaultMessage();
        assertEquals("Email не может быть пустым", message);
    }

    @Test
    void postUser_WithNullEmail_ShouldThrowException() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("email").getDefaultMessage();
        assertEquals("Email не может быть null", message);
    }

    @Test
    void postUser_WithEmailWithoutAtSymbol_ShouldThrowException() {
        User user = new User();
        user.setEmail("userexample.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("email").getDefaultMessage();
        assertEquals("Email должен содержать символ @ и быть корректным", message);
    }

    @Test
    void postUser_WithEmptyLogin_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("login").getDefaultMessage();
        assertEquals("Логин не может быть пустым", message);
    }

    @Test
    void postUser_WithBlankLogin_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("   ");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("login").getDefaultMessage();
        assertEquals("Логин не может быть пустым", message);
    }

    @Test
    void postUser_WithNullLogin_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(null);
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("login").getDefaultMessage();
        assertEquals("Логин не может быть null", message);
    }

    @Test
    void postUser_WithLoginContainingSpace_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("invalid login");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.postUser(user)
        );

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithBlankName_ShouldUseLoginAsName() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("");
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithNullName_ShouldUseLoginAsName() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName(null);
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithValidName_ShouldKeepName() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertEquals("Valid Name", result.getName());
    }

    @Test
    void postUser_WithBirthdayInFuture_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("birthday").getDefaultMessage();
        assertEquals("Дата рождения не может быть в будущем", message);
    }

    @Test
    void postUser_WithBirthdayExactlyToday_ShouldSucceed() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now());

        User result = userController.postUser(user);

        assertNotNull(result);
    }

    @Test
    void postUser_WithBirthdayInPast_ShouldSucceed() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertNotNull(result);
    }

    @Test
    void postUser_WithNullBirthday_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(null);

        MethodArgumentNotValidException exception = assertThrows(
                MethodArgumentNotValidException.class,
                () -> userController.postUser(user)
        );

        String message = exception.getBindingResult().getFieldError("birthday").getDefaultMessage();
        assertEquals("Дата рождения не может быть null", message);
    }

    @Test
    void putUser_WithValidExistingUser_ShouldUpdate() throws ValidationException {
        // Создаем пользователя
        User user = new User();
        user.setEmail("original@example.com");
        user.setLogin("originalLogin");
        user.setName("Original Name");
        user.setBirthday(LocalDate.now().minusYears(20));
        User created = userController.postUser(user);

        // Обновляем пользователя
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

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.putUser(user)
        );

        assertEquals("Пост с id = 999 не найден", exception.getMessage());
    }

    @Test
    void putUser_WithNullId_ShouldThrowException() {
        User user = new User();
        user.setId(null);
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.now());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.putUser(user)
        );

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws ValidationException {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.now().minusYears(20));
        userController.postUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.now().minusYears(25));
        userController.postUser(user2);

        var allUsers = userController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyCollection() {
        UserController freshController = new UserController();
        var allUsers = freshController.getAllUsers();
        assertTrue(allUsers.isEmpty());
    }

    @Test
    void userIdGeneration_ShouldBeIncremental() throws ValidationException {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.now().minusYears(20));
        User created1 = userController.postUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.now().minusYears(25));
        User created2 = userController.postUser(user2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ ГРАНИЧНЫЕ ТЕСТЫ ====================

    @Test
    void postFilm_MultipleFilms_ShouldHaveUniqueIds() throws ValidationException {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Desc 1");
        film1.setReleaseDate(LocalDate.now());
        film1.setDuration(90);
        Film created1 = filmController.postFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Desc 2");
        film2.setReleaseDate(LocalDate.now());
        film2.setDuration(120);
        Film created2 = filmController.postFilm(film2);

        assertNotEquals(created1.getId(), created2.getId());
    }

    @Test
    void postUser_MultipleUsers_ShouldHaveUniqueIds() throws ValidationException {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.now().minusYears(20));
        User created1 = userController.postUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.now().minusYears(25));
        User created2 = userController.postUser(user2);

        assertNotEquals(created1.getId(), created2.getId());
    }

    @Test
    void postFilm_WithDescriptionBoundary200_ShouldSucceed() throws ValidationException {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film result = filmController.postFilm(film);

        assertNotNull(result);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void postFilm_WithDescriptionBoundary201_ShouldThrowException() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        assertThrows(MethodArgumentNotValidException.class,
                () -> filmController.postFilm(film));
    }

    @Test
    void postUser_WithLoginBoundary_SingleCharacter() throws ValidationException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("a");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertNotNull(result);
        assertEquals("a", result.getLogin());
    }

    @Test
    void postUser_WithEmailBoundary_MinimalValidEmail() throws ValidationException {
        User user = new User();
        user.setEmail("a@b.ru");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        User result = userController.postUser(user);

        assertNotNull(result);
        assertEquals("a@b.ru", result.getEmail());
    }

    @Test
    void postUser_WithWhitespaceInLogin_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login with spaces");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.postUser(user)
        );

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void postUser_WithLoginStartingWithSpace_ShouldThrowException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(" login");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().minusYears(20));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.postUser(user)
        );

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }
}
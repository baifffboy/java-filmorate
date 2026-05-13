package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FilmorateApplicationUsersTests {

    @Autowired
    private UserController userController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    private User createValidUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    void postUser_WithValidUser_ShouldSucceed() throws ValidationException {
        User user = createValidUser("user@example.com", "validLogin", "Valid Name", LocalDate.now().minusYears(20));

        UserDto result = userController.postUser(user);

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

        UserDto result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithNullName_ShouldUseLoginAsName() throws ValidationException {
        User user = createValidUser("user@example.com", "validLogin", null, LocalDate.now().minusYears(20));

        UserDto result = userController.postUser(user);

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
        UserDto created = userController.postUser(user);

        UserDto result = userController.getUserById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("user@test.com", result.getEmail());
    }

    @Test
    void getUserById_WithNonExistentId_ShouldThrowNotFound() {
        assertThrows(NotFoundException.class,
                () -> userController.getUserById(999L));
    }

    @Test
    void putUser_WithValidExistingUser_ShouldUpdate() throws ValidationException {
        User user = createValidUser("original@example.com", "originalLogin", "Original Name", LocalDate.now().minusYears(20));
        UserDto created = userController.postUser(user);

        User updatedUser = createValidUser("updated@example.com", "updatedLogin", "Updated Name", LocalDate.now().minusYears(25));
        updatedUser.setId(created.getId());

        UserDto result = userController.putUser(updatedUser);

        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
        assertEquals(LocalDate.now().minusYears(25), result.getBirthday());
    }

    @Test
    void putUser_WithNonExistentId_ShouldThrowException() {
        User user = createValidUser("test@test.com", "test", "Test", LocalDate.now());
        user.setId(999L);

        assertThrows(NotFoundException.class,
                () -> userController.putUser(user));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws ValidationException {
        User user1 = createValidUser("user1@example.com", "user1", "User One", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@example.com", "user2", "User Two", LocalDate.now().minusYears(25));
        userController.postUser(user1);
        userController.postUser(user2);

        Collection<UserDto> allUsers = userController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void addFriend_ShouldAddFriend() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserDto createdUser1 = userController.postUser(user1);
        UserDto createdUser2 = userController.postUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        UserDto refreshedUser1 = userController.getUserById(createdUser1.getId());
        assertTrue(refreshedUser1.getFriends().contains(createdUser2.getId()));
    }

    @Test
    void deleteFriend_ShouldRemoveFriend() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserDto createdUser1 = userController.postUser(user1);
        UserDto createdUser2 = userController.postUser(user2);
        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        userController.deleteFriend(createdUser1.getId(), createdUser2.getId());

        UserDto refreshedUser1 = userController.getUserById(createdUser1.getId());
        assertFalse(refreshedUser1.getFriends().contains(createdUser2.getId()));
    }

    @Test
    void getFriends_ShouldReturnUserFriends() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserDto createdUser1 = userController.postUser(user1);
        UserDto createdUser2 = userController.postUser(user2);
        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        var friends = userController.getFriends(createdUser1.getId());

        assertEquals(1, friends.size());
    }

    @Test
    void getCommonFriends_ShouldReturnCommonFriends() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User common = createValidUser("common@test.com", "common", "Common", LocalDate.now().minusYears(20));

        UserDto createdUser1 = userController.postUser(user1);
        UserDto createdUser2 = userController.postUser(user2);
        UserDto createdCommon = userController.postUser(common);

        userController.addFriend(createdUser1.getId(), createdCommon.getId());
        userController.addFriend(createdUser2.getId(), createdCommon.getId());

        var commonFriends = userController.commonFriend(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
    }
}
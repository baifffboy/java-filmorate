package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationUsersTests {

    private UserController userController;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userController = new UserController(new UserService(userStorage));
    }

    // ==================== ТЕСТЫ ДЛЯ POST USER ====================

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

    // ==================== ТЕСТЫ ДЛЯ GET USER BY ID ====================

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
    void getUserById_WithNonExistentId_ShouldThrowNotFound() {
        assertThrows(ResponseStatusException.class,
                () -> userController.getUserById(999L));
    }

    // ==================== ТЕСТЫ ДЛЯ PUT USER ====================

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

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userController.putUser(user)
        );

        assertEquals("Пост с id = 999 не найден", exception.getReason());
    }

    // ==================== ТЕСТЫ ДЛЯ GET ALL USERS ====================

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws ValidationException {
        User user1 = createValidUser("user1@example.com", "user1", "User One", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@example.com", "user2", "User Two", LocalDate.now().minusYears(25));
        userController.postUser(user1);
        userController.postUser(user2);

        Collection<User> allUsers = userController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    // ==================== ТЕСТЫ ДЛЯ FRIENDS ====================

    @Test
    void addFriend_ShouldAddToBothUsersFriends() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);
        User createdUser2 = userController.postUser(user2);

        var result = userController.addFriend(createdUser1.getId(), createdUser2.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(createdUser1.getId())));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(createdUser2.getId())));

        // Проверяем, что дружба взаимная
        User refreshedUser1 = userController.getUserById(createdUser1.getId());
        User refreshedUser2 = userController.getUserById(createdUser2.getId());
        assertTrue(refreshedUser1.getFriends().contains(createdUser2.getId()));
        assertTrue(refreshedUser2.getFriends().contains(createdUser1.getId()));
    }

    @Test
    void addFriend_WithNonExistentUser_ShouldThrowException() throws ValidationException {
        User user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        User createdUser1 = userController.postUser(user1);

        assertThrows(ResponseStatusException.class,
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
        User refreshedUser1 = userController.getUserById(createdUser1.getId());
        User refreshedUser2 = userController.getUserById(createdUser2.getId());
        assertFalse(refreshedUser1.getFriends().contains(createdUser2.getId()));
        assertFalse(refreshedUser2.getFriends().contains(createdUser1.getId()));
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
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(createdUser2.getId())));
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
        assertTrue(commonFriends.stream().anyMatch(u -> u.getId().equals(createdCommon.getId())));
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

    private User createValidUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }
}

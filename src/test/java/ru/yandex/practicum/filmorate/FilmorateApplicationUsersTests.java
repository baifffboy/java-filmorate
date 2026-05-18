package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.users.UserCreateRequest;
import ru.yandex.practicum.filmorate.dto.users.UserResponse;
import ru.yandex.practicum.filmorate.dto.users.UserUpdateRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/cleanAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class FilmorateApplicationUsersTests {

    @Autowired
    private UserController userController;

    private UserCreateRequest createValidUser(String email, String login, String name, LocalDate birthday) {
        UserCreateRequest user = new UserCreateRequest();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    private UserUpdateRequest updateValidUser(String updateEmail, String updatedLogin, String updatedName, LocalDate updateBirthday) {
        UserUpdateRequest updateUser = new UserUpdateRequest();
        updateUser.setEmail(updateEmail);
        updateUser.setLogin(updatedLogin);
        updateUser.setName(updatedName);
        updateUser.setBirthday(updateBirthday);
        return updateUser;
    }

    @Test
    void postUser_WithValidUser_ShouldSucceed() throws ValidationException {
        UserCreateRequest user = createValidUser("user@example.com", "validLogin", "Valid Name", LocalDate.now().minusYears(20));

        UserResponse result = userController.postUser(user);

        assertNotNull(result.getId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("validLogin", result.getLogin());
        assertEquals("Valid Name", result.getName());
        assertNotNull(result.getFriends());
        assertTrue(result.getFriends().isEmpty());
    }

    @Test
    void postUser_WithBlankName_ShouldUseLoginAsName() throws ValidationException {
        UserCreateRequest user = createValidUser("user@example.com", "validLogin", "", LocalDate.now().minusYears(20));

        UserResponse result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithNullName_ShouldUseLoginAsName() throws ValidationException {
        UserCreateRequest user = createValidUser("user@example.com", "validLogin", null, LocalDate.now().minusYears(20));

        UserResponse result = userController.postUser(user);

        assertEquals("validLogin", result.getName());
    }

    @Test
    void postUser_WithLoginContainingSpace_ShouldThrowException() {
        UserCreateRequest user = createValidUser("user@example.com", "invalid login", "Valid Name", LocalDate.now().minusYears(20));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.postUser(user)
        );

        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() throws ValidationException {
        UserCreateRequest user = createValidUser("user@test.com", "user1", "User Name", LocalDate.now().minusYears(20));
        UserResponse created = userController.postUser(user);

        UserResponse result = userController.getUserById(created.getId());

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
        UserCreateRequest user = createValidUser("original@example.com", "originalLogin", "Original Name", LocalDate.now().minusYears(20));
        UserResponse created = userController.postUser(user);

        UserUpdateRequest updatedUser = updateValidUser("updated@example.com", "updatedLogin", "Updated Name", LocalDate.now().minusYears(25));
        updatedUser.setId(created.getId());

        UserResponse result = userController.putUser(updatedUser);

        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
        assertEquals(LocalDate.now().minusYears(25), result.getBirthday());
    }

    @Test
    void putUser_WithNonExistentId_ShouldThrowException() {
        UserUpdateRequest user = updateValidUser("test@test.com", "test", "Test", LocalDate.now());
        user.setId(999L);
        assertThrows(NotFoundException.class,
                () -> userController.putUser(user));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws ValidationException {
        UserCreateRequest user1 = createValidUser("user1@example.com", "user1", "User One", LocalDate.now().minusYears(20));
        UserCreateRequest user2 = createValidUser("user2@example.com", "user2", "User Two", LocalDate.now().minusYears(25));
        userController.postUser(user1);
        userController.postUser(user2);

        Collection<UserResponse> allUsers = userController.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void addFriend_ShouldAddFriend() throws ValidationException {
        UserCreateRequest user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        UserCreateRequest user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserResponse createdUser1 = userController.postUser(user1);
        UserResponse createdUser2 = userController.postUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        UserResponse refreshedUser1 = userController.getUserById(createdUser1.getId());
        assertTrue(refreshedUser1.getFriends().contains(createdUser2.getId()));
    }

    @Test
    void deleteFriend_ShouldRemoveFriend() throws ValidationException {
        UserCreateRequest user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        UserCreateRequest user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserResponse createdUser1 = userController.postUser(user1);
        UserResponse createdUser2 = userController.postUser(user2);
        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        userController.deleteFriend(createdUser1.getId(), createdUser2.getId());

        UserResponse refreshedUser1 = userController.getUserById(createdUser1.getId());
        assertFalse(refreshedUser1.getFriends().contains(createdUser2.getId()));
    }

    @Test
    void getFriends_ShouldReturnUserFriends() throws ValidationException {
        UserCreateRequest user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        UserCreateRequest user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserResponse createdUser1 = userController.postUser(user1);
        UserResponse createdUser2 = userController.postUser(user2);
        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        var friends = userController.getFriends(createdUser1.getId());

        assertEquals(1, friends.size());
    }

    @Test
    void getCommonFriends_ShouldReturnCommonFriends() throws ValidationException {
        UserCreateRequest user1 = createValidUser("user1@test.com", "user1", "User1", LocalDate.now().minusYears(20));
        UserCreateRequest user2 = createValidUser("user2@test.com", "user2", "User2", LocalDate.now().minusYears(20));
        UserCreateRequest common = createValidUser("common@test.com", "common", "Common", LocalDate.now().minusYears(20));

        UserResponse createdUser1 = userController.postUser(user1);
        UserResponse createdUser2 = userController.postUser(user2);
        UserResponse createdCommon = userController.postUser(common);

        userController.addFriend(createdUser1.getId(), createdCommon.getId());
        userController.addFriend(createdUser2.getId(), createdCommon.getId());

        var commonFriends = userController.commonFriend(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
    }
}
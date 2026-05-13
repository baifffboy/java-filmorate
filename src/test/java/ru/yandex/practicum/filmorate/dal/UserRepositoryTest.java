package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/schema.sql", "/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(jdbcTemplate, new UserRowMapper());
    }

    private User createTestUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    void testFindUserById_WhenUserExists_ShouldReturnUser() {
        User savedUser = userRepository.save(createTestUser("test@example.com", "testLogin", "Test Name", LocalDate.of(1990, 1, 1)));

        Optional<User> userOptional = userRepository.findById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", savedUser.getId())
                );
    }

    @Test
    void testFindUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> userOptional = userRepository.findById(999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    void testFindAllUsers_ShouldReturnAllUsers() {
        userRepository.save(createTestUser("user1@test.com", "user1", "User One", LocalDate.of(1990, 1, 1)));
        userRepository.save(createTestUser("user2@test.com", "user2", "User Two", LocalDate.of(1995, 2, 2)));

        Collection<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void testSaveUser_ShouldGenerateId() {
        User user = createTestUser("save@test.com", "saveLogin", "Save Name", LocalDate.of(1985, 5, 5));

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    void testUpdateUser_ShouldUpdateFields() {
        User savedUser = userRepository.save(createTestUser("original@test.com", "originalLogin", "Original Name", LocalDate.of(1990, 1, 1)));

        savedUser.setEmail("updated@test.com");
        savedUser.setLogin("updatedLogin");
        savedUser.setName("Updated Name");

        User updatedUser = userRepository.update(savedUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getLogin()).isEqualTo("updatedLogin");
    }

    @Test
    void testDeleteUser_ShouldRemoveUser() {
        User savedUser = userRepository.save(createTestUser("delete@test.com", "deleteLogin", "Delete Name", LocalDate.of(1990, 1, 1)));

        boolean deleted = userRepository.delete(savedUser.getId());

        assertThat(deleted).isTrue();
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testFindUserByEmail_ShouldReturnUser() {
        userRepository.save(createTestUser("email@test.com", "emailLogin", "Email Name", LocalDate.of(1990, 1, 1)));

        Optional<User> userOptional = userRepository.findByEmail("email@test.com");

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getEmail()).isEqualTo("email@test.com");
    }

    @Test
    void testAddFriend_OneWayFriendship() {
        User user1 = userRepository.save(createTestUser("user1@friends.com", "user1", "User One", LocalDate.of(1990, 1, 1)));
        User user2 = userRepository.save(createTestUser("user2@friends.com", "user2", "User Two", LocalDate.of(1991, 2, 2)));

        userRepository.addFriend(user1.getId(), user2.getId());

        Collection<User> friendsOfUser1 = userRepository.findFriends(user1.getId());
        Collection<User> friendsOfUser2 = userRepository.findFriends(user2.getId());

        assertThat(friendsOfUser1).hasSize(1);
        assertThat(friendsOfUser1.iterator().next().getId()).isEqualTo(user2.getId());
        assertThat(friendsOfUser2).isEmpty();
    }

    @Test
    void testDeleteFriend_ShouldRemoveFriendship() {
        User user1 = userRepository.save(createTestUser("user1@delete.com", "user1", "User One", LocalDate.of(1990, 1, 1)));
        User user2 = userRepository.save(createTestUser("user2@delete.com", "user2", "User Two", LocalDate.of(1991, 2, 2)));
        userRepository.addFriend(user1.getId(), user2.getId());

        userRepository.deleteFriend(user1.getId(), user2.getId());

        Collection<User> friendsOfUser1 = userRepository.findFriends(user1.getId());
        assertThat(friendsOfUser1).isEmpty();
    }

    @Test
    void testFindFriends_ShouldReturnUserFriends() {
        User user1 = userRepository.save(createTestUser("main@friends.com", "main", "Main User", LocalDate.of(1990, 1, 1)));
        User user2 = userRepository.save(createTestUser("friend@friends.com", "friend", "Friend", LocalDate.of(1991, 2, 2)));
        userRepository.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = userRepository.findFriends(user1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(user2.getId());
    }

    @Test
    void testFindCommonFriends_ShouldReturnIntersection() {
        User user1 = userRepository.save(createTestUser("user1@common.com", "user1", "User One", LocalDate.of(1990, 1, 1)));
        User user2 = userRepository.save(createTestUser("user2@common.com", "user2", "User Two", LocalDate.of(1991, 2, 2)));
        User common = userRepository.save(createTestUser("common@common.com", "common", "Common", LocalDate.of(1992, 3, 3)));

        userRepository.addFriend(user1.getId(), common.getId());
        userRepository.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userRepository.findCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(common.getId());
    }
}
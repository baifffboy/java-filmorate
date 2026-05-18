package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/cleanUp.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/cleanAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class FilmRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmRepository filmRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        filmRepository = new FilmRepository(jdbcTemplate, new FilmRowMapper());
        userRepository = new UserRepository(jdbcTemplate, new UserRowMapper());
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        Set<GenreOfFilm> genres = new LinkedHashSet<>();
        genres.add(GenreOfFilm.COMEDY);
        film.setGenres(genres);
        film.setMpa(MotionPictureAssociation.PG13);
        return film;
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testFindFilmById_WhenFilmExists_ShouldReturnFilm() {
        Film savedFilm = filmRepository.save(createTestFilm());

        Optional<Film> filmOptional = filmRepository.findById(savedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", savedFilm.getId())
                );
    }

    @Test
    void testFindFilmById_WhenFilmDoesNotExist_ShouldReturnEmpty() {
        Optional<Film> filmOptional = filmRepository.findById(999L);
        assertThat(filmOptional).isEmpty();
    }

    @Test
    void testFindAllFilms_ShouldReturnAllFilms() {
        filmRepository.save(createTestFilm());
        filmRepository.save(createTestFilm());

        List<Film> films = filmRepository.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void testSaveFilm_ShouldGenerateId() {
        Film savedFilm = filmRepository.save(createTestFilm());

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getId()).isGreaterThan(0);
    }

    @Test
    void testUpdateFilm_ShouldUpdateFields() {
        Film savedFilm = filmRepository.save(createTestFilm());

        savedFilm.setName("Updated Name");
        savedFilm.setDescription("Updated Desc");
        savedFilm.setDuration(180);

        Film updatedFilm = filmRepository.update(savedFilm);
        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());

        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getDescription()).isEqualTo("Updated Desc");
    }

    @Test
    void testDeleteFilm_ShouldRemoveFilm() {
        Film savedFilm = filmRepository.save(createTestFilm());

        boolean deleted = filmRepository.delete(savedFilm.getId());

        assertThat(deleted).isTrue();
        Optional<Film> foundFilm = filmRepository.findById(savedFilm.getId());
        assertThat(foundFilm).isEmpty();
    }

    @Test
    void testAddLike_ShouldAddLikeToFilm() {
        User savedUser = userRepository.save(createTestUser());
        Film savedFilm = filmRepository.save(createTestFilm());

        filmRepository.addLike(savedFilm.getId(), savedUser.getId());

        List<Long> likes = filmRepository.findLikes(savedFilm.getId());
        assertThat(likes).hasSize(1);
        assertThat(likes.get(0)).isEqualTo(savedUser.getId());
    }

    @Test
    void testAddLike_DuplicateLike_ShouldNotAddAgain() {
        User savedUser = userRepository.save(createTestUser());
        Film savedFilm = filmRepository.save(createTestFilm());

        filmRepository.addLike(savedFilm.getId(), savedUser.getId());
        filmRepository.addLike(savedFilm.getId(), savedUser.getId());

        List<Long> likes = filmRepository.findLikes(savedFilm.getId());
        assertThat(likes).hasSize(1);
    }

    @Test
    void testDeleteLike_WhenLikeExists_ShouldRemoveLike() {
        User savedUser = userRepository.save(createTestUser());
        Film savedFilm = filmRepository.save(createTestFilm());
        filmRepository.addLike(savedFilm.getId(), savedUser.getId());

        filmRepository.deleteLike(savedFilm.getId(), savedUser.getId());

        List<Long> likes = filmRepository.findLikes(savedFilm.getId());
        assertThat(likes).isEmpty();
    }

    @Test
    void testDeleteLike_WhenLikeDoesNotExist_ShouldThrowException() {
        User savedUser = userRepository.save(createTestUser());
        Film savedFilm = filmRepository.save(createTestFilm());

        assertThatThrownBy(() -> filmRepository.deleteLike(savedFilm.getId(), savedUser.getId()))
                .isInstanceOf(ru.yandex.practicum.filmorate.exception.NotFoundException.class);
    }

    @Test
    void testFindLikes_ShouldReturnListOfUserIds() {
        User user1 = userRepository.save(createTestUser());
        User user2 = userRepository.save(createTestUser());
        Film savedFilm = filmRepository.save(createTestFilm());

        filmRepository.addLike(savedFilm.getId(), user1.getId());
        filmRepository.addLike(savedFilm.getId(), user2.getId());

        List<Long> likes = filmRepository.findLikes(savedFilm.getId());

        assertThat(likes).hasSize(2);
        assertThat(likes).containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void testFindPopular_ShouldReturnFilmsSortedByLikes() {
        User user1 = userRepository.save(createTestUser());
        User user2 = userRepository.save(createTestUser());
        User user3 = userRepository.save(createTestUser());

        Film film1 = filmRepository.save(createTestFilm());
        Film film2 = filmRepository.save(createTestFilm());
        Film film3 = filmRepository.save(createTestFilm());

        filmRepository.addLike(film2.getId(), user1.getId());
        filmRepository.addLike(film2.getId(), user2.getId());
        filmRepository.addLike(film2.getId(), user3.getId());

        filmRepository.addLike(film1.getId(), user1.getId());

        List<Film> popular = filmRepository.findPopular(10);

        assertThat(popular).hasSize(3);
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(2).getId()).isEqualTo(film3.getId());
    }

    @Test
    void testFindPopular_WithLimit_ShouldReturnLimitedResults() {
        for (int i = 0; i < 5; i++) {
            Film film = createTestFilm();
            film.setName("Film " + i);
            filmRepository.save(film);
        }

        List<Film> popular = filmRepository.findPopular(3);

        assertThat(popular).hasSize(3);
    }
}
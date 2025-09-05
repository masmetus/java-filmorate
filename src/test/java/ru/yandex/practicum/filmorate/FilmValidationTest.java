package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);

        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createFilm_ValidFilm_ShouldCreateSuccessfully() {
        Film createdFilm = filmService.create(testFilm);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
    }

    @Test
    void createFilm_ReleaseDateBefore1895_ShouldThrowValidationException() {
        testFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(testFilm));

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void createFilm_DuplicateName_ShouldThrowValidationException() {
        filmService.create(testFilm);

        Film duplicateFilm = new Film();
        duplicateFilm.setName("Test Film");
        duplicateFilm.setDescription("Another description");
        duplicateFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        duplicateFilm.setDuration(100);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.create(duplicateFilm));

        assertEquals("Фильм уже есть на сайте", exception.getMessage());
    }

    @Test
    void findFilmById_ExistingFilm_ShouldReturnFilm() {
        Film createdFilm = filmService.create(testFilm);
        Film foundFilm = filmService.findFilmById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void findFilmById_NonExistingFilm_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.findFilmById(999L));

        assertEquals("Фильм не найден.", exception.getMessage());
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        filmService.create(testFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        filmService.create(anotherFilm);

        Collection<Film> films = filmService.findAll();

        assertEquals(2, films.size());
    }

    @Test
    void patch_UpdateFilmName_ShouldUpdateSuccessfully() {
        Film createdFilm = filmService.create(testFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Film Name");

        Film updatedFilm = filmService.patch(updateFilm);

        assertEquals("Updated Film Name", updatedFilm.getName());
        assertEquals("Test Description", updatedFilm.getDescription()); // остальные поля не изменились
    }

    @Test
    void patch_UpdateWithDuplicateName_ShouldThrowValidationException() {
        Film firstFilm = filmService.create(testFilm);

        Film secondFilm = new Film();
        secondFilm.setName("Second Film");
        secondFilm.setDescription("Second description");
        secondFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        secondFilm.setDuration(90);
        Film createdSecondFilm = filmService.create(secondFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdSecondFilm.getId());
        updateFilm.setName("Test Film"); // пытаемся изменить на имя первого фильма

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.patch(updateFilm));

        assertEquals("Фильм с таким названием уже существует", exception.getMessage());
    }

    @Test
    void update_FullUpdate_ShouldUpdateAllFields() {
        Film createdFilm = filmService.create(testFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Fully Updated Film");
        updateFilm.setDescription("Fully updated description");
        updateFilm.setReleaseDate(LocalDate.of(2005, 5, 5));
        updateFilm.setDuration(150);

        Film updatedFilm = filmService.update(updateFilm);

        assertEquals("Fully Updated Film", updatedFilm.getName());
        assertEquals("Fully updated description", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2005, 5, 5), updatedFilm.getReleaseDate());
        assertEquals(150, updatedFilm.getDuration());
    }

    @Test
    void addLike_ValidFilmAndUser_ShouldAddLike() {
        Film createdFilm = filmService.create(testFilm);
        User createdUser = userStorage.create(testUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());

        Film filmAfterLike = filmService.findFilmById(createdFilm.getId());
        assertTrue(filmAfterLike.getLikedUsersIds().contains(createdUser.getId()));
    }

    @Test
    void addLike_NonExistingFilm_ShouldThrowNotFoundException() {
        User createdUser = userStorage.create(testUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.addLike(999L, createdUser.getId()));

        assertEquals("Фильм не найден.", exception.getMessage());
    }

    @Test
    void addLike_NonExistingUser_ShouldThrowNotFoundException() {
        Film createdFilm = filmService.create(testFilm);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.addLike(createdFilm.getId(), 999L));

        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void removeLike_ValidLike_ShouldRemoveLike() {
        Film createdFilm = filmService.create(testFilm);
        User createdUser = userStorage.create(testUser);

        filmService.addLike(createdFilm.getId(), createdUser.getId());
        filmService.removeLike(createdFilm.getId(), createdUser.getId());

        Film filmAfterRemove = filmService.findFilmById(createdFilm.getId());
        assertFalse(filmAfterRemove.getLikedUsersIds().contains(createdUser.getId()));
    }

    @Test
    void findPopularFilms_ShouldReturnOrderedByLikes() {
        // Создаем фильмы
        Film film1 = filmService.create(testFilm);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(100);
        Film createdFilm2 = filmService.create(film2);

        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2002, 1, 1));
        film3.setDuration(110);
        Film createdFilm3 = filmService.create(film3);

        // Создаем пользователей
        User user1 = userStorage.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userStorage.create(user2);

        // Добавляем лайки: film1 - 2 лайка, film2 - 1 лайк, film3 - 0 лайков
        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), createdUser2.getId());
        filmService.addLike(createdFilm2.getId(), user1.getId());

        Collection<Film> popularFilms = filmService.findPopularFilms(2);

        assertEquals(2, popularFilms.size());
        // Первый фильм должен быть с наибольшим количеством лайков
        assertEquals(film1.getId(), popularFilms.iterator().next().getId());
    }
}
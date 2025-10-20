package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final LocalDate FIRST_FILM_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        User user = userStorage.findUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.findFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        User user = userStorage.findUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> findPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
        return filmStorage.findLikedFilm(count);
    }

    public Film findFilmById(Long id) {
        Film film = filmStorage.findFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }

        return film;
    }

    public Film create(Film film) {
        log.info("POST /films - Создание фильма: {}", film.getName());

        validateFilmForCreate(film);

        filmStorage.create(film);
        log.info("Фильм создан успешно: ID={}, Name={}", film.getId(), film.getName());

        return film;
    }

    public Film patch(Film newFilm) {
        log.info("PATCH /films - Обновление фильма: ID={}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: ID не указан");
            throw new ValidationException("Id должен быть указан.");
        }
        Film oldFilm = filmStorage.findFilmById(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Ошибка: Фильм с ID={} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден.");
        }

        validateFilmForUpdate(newFilm, oldFilm);

        if (newFilm.getName() != null && !newFilm.getName().isBlank()
                && !newFilm.getName().equals(oldFilm.getName())) {
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getDuration() > 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }

        log.info("Фильм обновлен успешно: ID={}", oldFilm.getId());
        return filmStorage.update(oldFilm);
    }


    public Film update(Film newFilm) {
        log.info("PUT /films - Полное обновление фильма: ID={}", newFilm.getId());

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан.");
        }

        Film oldFilm = filmStorage.findFilmById(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден.");
        }

        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Фильм обновлен успешно: ID={}", oldFilm.getId());
        return filmStorage.update(oldFilm);
    }


    private void validateFilmForCreate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE_RELEASE)) {
            log.warn("Ошибка валидации: Дата релиза {} раньше допустимой {}",
                    film.getReleaseDate(), FIRST_FILM_DATE_RELEASE);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (filmStorage.isExistingFilm(film.getName())) {
            log.warn("Ошибка валидации: Фильм '{}' уже существует", film.getName());
            throw new ValidationException("Фильм уже есть на сайте");
        }
    }

    private void validateFilmForUpdate(Film newFilm, Film oldFilm) {
        if (!newFilm.getName().equals(oldFilm.getName()) && filmStorage.isExistingFilm(newFilm.getName())) {
            log.warn("Ошибка: Фильм с названием={} уже существует", newFilm.getName());
            throw new ValidationException("Фильм с таким названием уже существует");
        }


        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(FIRST_FILM_DATE_RELEASE)) {
            log.warn("Ошибка: дата релиза={} раньше разрешённой={}", newFilm.getReleaseDate(), FIRST_FILM_DATE_RELEASE);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

    }
}

package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate FIRST_FILM_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();
    private final Set<String> existingFilms = new HashSet<>();


    @GetMapping
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films - Создание фильма: {}", film.getName());

        validateFilmForCreate(film);

        film.setId(getNextId());

        films.put(film.getId(), film);
        existingFilms.add(film.getName());

        log.info("Фильм создан успешно: ID={}, Name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films - Обновление фильма: ID={}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: ID не указан");
            throw new ValidationException("Id должен быть указан.");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Ошибка: Фильм с ID={} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден.");
        }

        Film oldFilm = films.get(newFilm.getId());
        validateFilmForUpdate(newFilm, oldFilm);

        if (!newFilm.getName().equals(oldFilm.getName())) {
            existingFilms.remove(oldFilm.getName());
            oldFilm.setName(newFilm.getName());
            existingFilms.add(newFilm.getName());
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
        return oldFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateFilmForCreate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE_RELEASE)) {
            log.warn("Ошибка валидации: Дата релиза {} раньше допустимой {}",
                    film.getReleaseDate(), FIRST_FILM_DATE_RELEASE);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (existingFilms.contains(film.getName())) {
            log.warn("Ошибка валидации: Фильм '{}' уже существует", film.getName());
            throw new ValidationException("Фильм уже есть на сайте");
        }
    }

    private void validateFilmForUpdate(Film newFilm, Film oldFilm) {
        if (!newFilm.getName().equals(oldFilm.getName()) && existingFilms.contains(newFilm.getName())) {
            log.warn("Ошибка: Фильм с названием={} уже существует", newFilm.getName());
            throw new ValidationException("Фильм с таким названием уже существует");
        }


        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(FIRST_FILM_DATE_RELEASE)) {
            log.warn("Ошибка: дата релиза={} раньше разрешённой={}", newFilm.getReleaseDate(), FIRST_FILM_DATE_RELEASE);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

    }

}


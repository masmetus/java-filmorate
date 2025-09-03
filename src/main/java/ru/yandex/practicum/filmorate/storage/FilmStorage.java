package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Collection<Film> findAll();

    public Film findFilmById(Long id);

    public Collection<Film> findLikedFilm(int count);

    public boolean isLiked(Long filmId, Long userId);

    public void addLike(Long filmId, Long userId);

    public Film create(Film film);

    public Film update(Film film);

    public boolean isExistingFilm(String filmName);

    void removeLike(Long filmId, Long userId);
}

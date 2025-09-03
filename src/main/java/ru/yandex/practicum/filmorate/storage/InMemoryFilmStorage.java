package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Set<String> existingFilms = new HashSet<>();

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Collection<Film> findLikedFilm(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt(film -> film.getLikedUsersIds().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film findFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        Long newId = getNextId();
        film.setId(newId);
        films.put(getNextId(), film);
        existingFilms.add(film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        if (oldFilm != null) {
            existingFilms.remove(oldFilm.getName());
        }

        films.put(film.getId(), oldFilm);
        existingFilms.add(film.getName());

        return film;
    }

    @Override
    public boolean isExistingFilm(String filmName) {
        return existingFilms.contains(filmName);
    }

    @Override
    public boolean isLiked(Long filmId, Long userId) {
        Film film = films.get(filmId);
        return film.getLikedUsersIds().contains(userId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikedUsersIds().add(userId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikedUsersIds().remove(userId);
        }
    }


    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}

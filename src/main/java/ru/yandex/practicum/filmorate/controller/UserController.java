package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final HashMap<Long, User> users = new HashMap<>();
    private final Set<String> existingEmails = new HashSet<>();


    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users - Создание пользователя: {}", user.getEmail());
        if (existingEmails.contains(user.getEmail())) {
            log.warn("Ошибка валидации: Email '{}' уже используется", user.getEmail());
            throw new ValidationException("Этот email уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());

        users.put(user.getId(), user);
        existingEmails.add(user.getEmail());

        log.info("Пользователь создан успешно: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("PUT /users - Обновление пользователя: ID={}", newUser.getId());
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: ID не указан");
            throw new ValidationException("Id должен быть указан.");
        }
        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка: Пользователь с ID={} не найден", newUser.getId());
            throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()
                && !newUser.getEmail().equals(oldUser.getEmail())) {
            if (existingEmails.contains(newUser.getEmail())) {
                log.warn("Ошибка валидации: Email '{}' уже используется", newUser.getEmail());
                throw new ValidationException("Этот email уже используется.");
            }
            existingEmails.remove(oldUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            existingEmails.add(newUser.getEmail());
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            oldUser.setName(newUser.getLogin());
        } else {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь обновлен успешно: ID={}", oldUser.getId());
        return oldUser;
    }


    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

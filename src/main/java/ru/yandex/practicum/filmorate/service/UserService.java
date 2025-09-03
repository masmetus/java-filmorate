package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findUserById(Long id) {
        User user = userStorage.findUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    public User create(User user) {
        log.info("POST /users - Создание пользователя: {}", user.getEmail());
        validateUserForCreate(user);

        userStorage.create(user);

        log.info("Пользователь создан успешно: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }

    public User update(User newUser) {
        log.info("PUT /users - Обновление пользователя: ID={}", newUser.getId());

        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: ID не указан");
            throw new ValidationException("Id должен быть указан.");
        }

        User oldUser = userStorage.findUserById(newUser.getId());
        if (oldUser == null) {
            log.warn("Ошибка: Пользователь с ID={} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        validateUserForUpdate(newUser, oldUser);

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
        return userStorage.update(oldUser);
    }


    private void validateUserForCreate(User user) {
        if (userStorage.isExistingEmail(user.getEmail())) {
            log.warn("Ошибка валидации: Email '{}' уже используется", user.getEmail());
            throw new ValidationException("Этот email уже используется");
        }
    }

    private void validateUserForUpdate(User newUser, User oldUser) {
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()
                && !newUser.getEmail().equals(oldUser.getEmail())) {
            if (userStorage.isExistingEmail(newUser.getEmail())) {
                log.warn("Ошибка валидации: Email '{}' уже используется", newUser.getEmail());
                throw new ValidationException("Этот email уже используется.");
            }
        }
    }
}

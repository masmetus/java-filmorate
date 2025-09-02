package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = new User();
        validUser.setEmail("test@mail.ru");
        validUser.setLogin("validlogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldValidateCorrectUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений");
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        validUser.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Email не может быть null");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        validUser.setEmail("   ");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Email не может быть пустым");
    }

    @Test
    void shouldFailWhenEmailInvalidFormat() {
        validUser.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Email должен быть валидным");
    }

    @Test
    void shouldFailWhenLoginIsNull() {
        validUser.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Логин не может быть null");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        validUser.setLogin("   ");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Логин не может быть пустым");
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        validUser.setLogin("login with spaces");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Логин не может содержать пробелы");
    }

    @Test
    void shouldPassWhenNameIsNull() {
        validUser.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Name может быть null");
    }

    @Test
    void shouldPassWhenNameIsBlank() {
        validUser.setName("   ");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Name может быть пустым");
    }

    @Test
    void shouldFailWhenBirthdayIsFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Дата рождения не может быть в будущем");
    }

    @Test
    void shouldPassWhenBirthdayIsToday() {
        validUser.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Сегодняшняя дата допустима");
    }
}

package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void shouldValidateCorrectFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь нарушений");
    }

    @Test
    void shouldFailWhenNameIsNull() {
        validFilm.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Название не может быть null");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        validFilm.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Название не может быть пустым");
    }

    @Test
    void shouldPassWhenDescriptionIsNull() {
        validFilm.setDescription(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Описание может быть null");
    }

    @Test
    void shouldPassWhenDescriptionIsEmpty() {
        validFilm.setDescription("");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Описание может быть пустым");
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        validFilm.setDescription("A".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Описание не может быть длиннее 200 символов");
    }

    @Test
    void shouldPassWhenDescription200Chars() {
        validFilm.setDescription("A".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Описание в 200 символов допустимо");
    }

    @Test
    void shouldPassWhenReleaseDateIsNull() {
        validFilm.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Дата релиза может быть null");
    }

    @Test
    void shouldFailWhenDurationZero() {
        validFilm.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Длительность должна быть положительной");
    }

    @Test
    void shouldFailWhenDurationNegative() {
        validFilm.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Длительность не может быть отрицательной");
    }

    @Test
    void shouldPassWhenDurationPositive() {
        validFilm.setDuration(1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Положительная длительность допустима");
    }
}

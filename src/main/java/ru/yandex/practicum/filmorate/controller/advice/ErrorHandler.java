package ru.yandex.practicum.filmorate.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.DTO.ErrorResponse;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException e) {
        log.warn("NotFoundException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "Запрашиваемый ресурс не найден.",
                e.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationException(final ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "Некорректное значение параметра.",
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUnexpectedException(final RuntimeException e) {
        log.error("Неожиданная ошибка", e);
        ErrorResponse errorResponse = new ErrorResponse(
                "Возникла непредвиденная ошибка.",
                ""
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

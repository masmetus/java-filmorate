package ru.yandex.practicum.filmorate.model.DTO;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String error;
    private final String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }

}

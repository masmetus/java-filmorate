package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@EqualsAndHashCode(of = "id")
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть больше 200 символов")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Min(value = 1, message = "Продолжительность должна быть положительной")
    private int duration;

    //Изменится при появлении БД
    private Set<Long> likedUsersIds = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    //Изменится при появлении БД, здесь будет id
    private MpaRating rating;
}

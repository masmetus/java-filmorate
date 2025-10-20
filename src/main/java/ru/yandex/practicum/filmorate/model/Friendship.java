package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;

public class Friendship {

    private Long id;

    private Long requesterId;

    private Long addresseeId;

    private FriendshipStatus status;
}

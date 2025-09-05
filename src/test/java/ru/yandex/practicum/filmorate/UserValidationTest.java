package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private UserService userService;
    private InMemoryUserStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUser_ValidUser_ShouldCreateSuccessfully() {
        User createdUser = userService.create(testUser);

        assertNotNull(createdUser.getId());
        assertEquals("test@mail.ru", createdUser.getEmail());
        assertEquals("testlogin", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
    }

    @Test
    void createUser_WithBlankName_ShouldSetLoginAsName() {
        testUser.setName("");

        User createdUser = userService.create(testUser);

        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowValidationException() {
        userService.create(testUser);

        User duplicateUser = new User();
        duplicateUser.setEmail("test@mail.ru"); // тот же email
        duplicateUser.setLogin("differentlogin");
        duplicateUser.setName("Different User");
        duplicateUser.setBirthday(LocalDate.of(1991, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(duplicateUser));

        assertEquals("Этот email уже используется", exception.getMessage());
    }

    @Test
    void findUserById_ExistingUser_ShouldReturnUser() {
        User createdUser = userService.create(testUser);
        User foundUser = userService.findUserById(createdUser.getId());

        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("test@mail.ru", foundUser.getEmail());
    }

    @Test
    void findUserById_NonExistingUser_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findUserById(999L));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updateUser_ValidUpdate_ShouldUpdateSuccessfully() {
        User createdUser = userService.create(testUser);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("updated@mail.ru");
        updateUser.setLogin("updatedlogin");
        updateUser.setName("Updated User");
        updateUser.setBirthday(LocalDate.of(1995, 1, 1));

        User updatedUser = userService.update(updateUser);

        assertEquals("updated@mail.ru", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
        assertEquals("Updated User", updatedUser.getName());
        assertEquals(LocalDate.of(1995, 1, 1), updatedUser.getBirthday());
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowValidationException() {
        User firstUser = userService.create(testUser);

        User secondUser = new User();
        secondUser.setEmail("second@mail.ru");
        secondUser.setLogin("secondlogin");
        secondUser.setName("Second User");
        secondUser.setBirthday(LocalDate.of(1991, 1, 1));
        User createdSecondUser = userService.create(secondUser);

        User updateUser = new User();
        updateUser.setId(createdSecondUser.getId());
        updateUser.setEmail("test@mail.ru"); // пытаемся изменить на email первого пользователя
        updateUser.setLogin("secondlogin");
        updateUser.setName("Second User");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.update(updateUser));

        assertEquals("Этот email уже используется", exception.getMessage());
    }

    @Test
    void addFriends_ValidUsers_ShouldAddFriends() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriends(user1.getId(), createdUser2.getId());

        User updatedUser1 = userService.findUserById(user1.getId());
        User updatedUser2 = userService.findUserById(createdUser2.getId());

        assertTrue(updatedUser1.getFriendIds().contains(createdUser2.getId()));
        assertTrue(updatedUser2.getFriendIds().contains(user1.getId()));
    }

    @Test
    void addFriends_AddSelfAsFriend_ShouldThrowValidationException() {
        User user = userService.create(testUser);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.addFriends(user.getId(), user.getId()));

        assertEquals("Нельзя добавить себя в друзья", exception.getMessage());
    }

    @Test
    void removeFriends_ValidFriends_ShouldRemoveFriends() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriends(user1.getId(), createdUser2.getId());
        userService.removeFriends(user1.getId(), createdUser2.getId());

        User updatedUser1 = userService.findUserById(user1.getId());
        User updatedUser2 = userService.findUserById(createdUser2.getId());

        assertFalse(updatedUser1.getFriendIds().contains(createdUser2.getId()));
        assertFalse(updatedUser2.getFriendIds().contains(user1.getId()));
    }

    @Test
    void getFriends_UserWithFriends_ShouldReturnFriendsList() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        User user3 = new User();
        user3.setEmail("user3@mail.ru");
        user3.setLogin("user3login");
        user3.setName("User 3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));
        User createdUser3 = userService.create(user3);

        userService.addFriends(user1.getId(), createdUser2.getId());
        userService.addFriends(user1.getId(), createdUser3.getId());

        List<User> friends = userService.getFriends(user1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(createdUser2.getId())));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(createdUser3.getId())));
    }

    @Test
    void getCommonFriends_UsersWithCommonFriends_ShouldReturnCommonFriends() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@mail.ru");
        commonFriend.setLogin("commonlogin");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        User createdCommonFriend = userService.create(commonFriend);

        // Добавляем общего друга
        userService.addFriends(user1.getId(), createdCommonFriend.getId());
        userService.addFriends(createdUser2.getId(), createdCommonFriend.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(createdCommonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userService.create(testUser);

        User anotherUser = new User();
        anotherUser.setEmail("another@mail.ru");
        anotherUser.setLogin("anotherlogin");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1991, 1, 1));
        userService.create(anotherUser);

        Collection<User> users = userService.findAll();

        assertEquals(2, users.size());
    }
}

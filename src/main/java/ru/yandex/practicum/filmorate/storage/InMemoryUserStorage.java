package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> existingEmails = new HashSet<>();


    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(Long id) {
        return users.get(id);
    }

    @Override
    public User create(User user) {
        Long id = getNextId();
        user.setId(id);

        users.put(user.getId(), user);
        existingEmails.add(user.getEmail());

        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = users.get(user.getId());
        if (oldUser != null) {
            existingEmails.remove(oldUser.getEmail());
        }

        users.put(getNextId(), user);
        existingEmails.add(user.getEmail());

        return oldUser;
    }

    @Override
    public boolean isExistingEmail(String email) {
        return existingEmails.contains(email);
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

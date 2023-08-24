package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.User;

import java.util.Collection;
import java.util.UUID;

public interface UserService {
    User createUser(User user);

    User findById(UUID id);

    Collection<User> findByName(String name);

    User findByEmail(String email);

    User updateUser(User user);

    void deleteUser(UUID id);

    Collection<User> getAll();
}

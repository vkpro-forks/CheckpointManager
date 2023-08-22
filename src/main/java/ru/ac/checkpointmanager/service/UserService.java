package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.User;

import java.util.Collection;

public interface UserService {
    User createUser(User user);

    User findById(Long id);

    Collection<User> findByName(String name);
}

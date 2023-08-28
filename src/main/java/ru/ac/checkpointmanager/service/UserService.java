package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.User;

import java.util.Collection;
import java.util.UUID;

public interface UserService {
    User createUser(User user);

    User findById(UUID id);

    Collection<User> findByName(String name);

    User findByEmail(String email);

    User updateUser(User user);

    User updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Collection<User> getAll();

    User convertToUser(UserDTO userDTO);

    UserDTO convertToUserDTO(User user);
}

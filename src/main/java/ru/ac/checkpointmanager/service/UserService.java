package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.dto.UserPhoneDTO;

import java.util.Collection;
import java.util.UUID;

public interface UserService {
    UserPhoneDTO createUser(UserPhoneDTO userDTO);

    UserDTO findById(UUID id);

    Collection<UserDTO> findByName(String name);

    UserDTO updateUser(UserDTO userDTO);

    UserDTO updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Collection<UserDTO> getAll();

    // метод обращается к сету номеров юзера и добавляет только поле НОМЕРА в лист
    Collection<String> findUsersPhoneNumbers(UUID userId);
}

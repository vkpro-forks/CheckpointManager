package ru.ac.checkpointmanager.service.user;

import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.enums.Role;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDTO findById(UUID id);

    List<TerritoryDTO> findTerritoriesByUserId(UUID userId);

    Collection<UserDTO> findByName(String name);

    UserDTO updateUser(UserDTO userDTO);

    void changePassword(ChangePasswordRequest request, Principal connectedUser);

    void changeRole(UUID id, Role role, Principal connectedUser);

    UserDTO updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Collection<UserDTO> getAll();

    Collection<String> findUsersPhoneNumbers(UUID userId);
}

package ru.ac.checkpointmanager.service.user;

import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO findById(UUID id);

    List<TerritoryDTO> findTerritoriesByUserId(UUID userId);

    Collection<UserResponseDTO> findByName(String name);

    UserResponseDTO updateUser(UserPutDTO userPutDTO);

    void changePassword(ChangePasswordRequest request, Principal connectedUser);

    void changeRole(UUID id, Role role, Principal connectedUser);

    UserResponseDTO updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Collection<UserResponseDTO> getAll();

    Collection<String> findUsersPhoneNumbers(UUID userId);

    public void assignAvatarToUser(UUID userId, Avatar avatar);
}
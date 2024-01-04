package ru.ac.checkpointmanager.service.user;

import ru.ac.checkpointmanager.dto.user.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.user.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.user.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.ConfirmChangeEmail;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO findById(UUID id);

    User findUserById(UUID id);

    List<TerritoryDTO> findTerritoriesByUserId(UUID userId);

    Collection<UserResponseDTO> findByName(String name);

    UserResponseDTO findByEmail(String email);

    UserResponseDTO updateUser(UserPutDTO userPutDTO);

    void changePassword(ChangePasswordRequest request);

    ConfirmChangeEmail changeEmail(ChangeEmailRequest request);

    AuthenticationResponse confirmEmail(String token);

    void changeRole(UUID id, Role role);

    UserResponseDTO updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Collection<UserResponseDTO> getAll();

    Collection<String> findUsersPhoneNumbers(UUID userId);

    void assignAvatarToUser(UUID userId, Avatar avatar);

    User findByPassId(UUID passId);
}

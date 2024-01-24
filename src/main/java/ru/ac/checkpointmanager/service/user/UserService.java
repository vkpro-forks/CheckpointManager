package ru.ac.checkpointmanager.service.user;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.EmailConfirmationDTO;
import ru.ac.checkpointmanager.dto.user.NewEmailDTO;
import ru.ac.checkpointmanager.dto.user.NewPasswordDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.dto.user.UserUpdateDTO;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
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

    UserResponseDTO updateUser(UserUpdateDTO userUpdateDTO);

    void changePassword(NewPasswordDTO request);

    EmailConfirmationDTO changeEmail(NewEmailDTO request);

    AuthResponseDTO confirmEmail(String token);

    void changeRole(UUID id, Role role);

    UserResponseDTO updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Page<UserResponseDTO> getAll(PagingParams pagingParams);

    Collection<String> findUsersPhoneNumbers(UUID userId);

    void assignAvatarToUser(UUID userId, Avatar avatar);

    User findByPassId(UUID passId);
}

package ru.ac.checkpointmanager.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.Mapper;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final Mapper mapper;
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO findById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        return mapper.toUserDTO(foundUser);
    }

    @Override
    public List<TerritoryDTO> findTerritoriesByUserId(UUID userId) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), userId);
        List<Territory> territories = userRepository.findTerritoriesByUserId(userId);
        if (territories.isEmpty()) {
            log.warn("Territory for User {} not found", userId);
            throw new TerritoryNotFoundException(String.format("Territory for User not found [user_id=%s]", userId));
        }
        return mapper.toTerritoriesDTO(territories);
    }

    @Override
    public Collection<UserResponseDTO> findByName(String name) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        Collection<UserResponseDTO> userResponseDTOS = mapper.toUsersDTO(userRepository
                .findUserByFullNameContainingIgnoreCase(name));

        if (userResponseDTOS.isEmpty()) {
            log.debug("here is no user with name containing {}", name);
            throw new UserNotFoundException("There is no user with name containing " + name);
        }
        return userResponseDTOS;
    }

    @Override
    public UserResponseDTO updateUser(UserPutDTO userPutDTO) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), userPutDTO.getId());
        User foundUser = userRepository.findById(userPutDTO.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User not found [id=%s]", userPutDTO.getId())));

        if (userPutDTO.getFullName() != null) {
            foundUser.setFullName(userPutDTO.getFullName());
        }

        if (userPutDTO.getMainNumber() != null && !userPutDTO.getMainNumber().isEmpty()) {
            if (!findUsersPhoneNumbers(userPutDTO.getId()).contains(cleanPhone(userPutDTO.getMainNumber()))) {
                log.warn("Phone {} does not exist", userPutDTO.getMainNumber());
                throw new PhoneNumberNotFoundException(String.format
                        ("Phone number %s does not exist", userPutDTO.getMainNumber()));
            }
            foundUser.setMainNumber(cleanPhone(userPutDTO.getMainNumber()));
        }

        userRepository.save(foundUser);
        log.debug("User {} saved", foundUser.getId());

        return mapper.toUserDTO(foundUser);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) { //  Principal представляет собой пользователя, который был идентифицирован в результате процесса аутентификации
        log.debug("Method {}, Username - {}", MethodLog.getMethodName(), connectedUser.getName());
        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal(); // получаем юзера из connectedUser, который представляет текущего пользователя

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password not matched for {}", user.getEmail());
            throw new IllegalStateException("Current password not matched");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            log.warn("Passwords are not the same ");
            throw new IllegalStateException("Passwords are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.debug("Password for {} successfully changed", user.getEmail());
    }

    @Override
    public void changeRole(UUID id, Role role, Principal connectedUser) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (role == Role.ADMIN && !user.getRole().equals(Role.ADMIN)) {
            log.error("Users with role {} do not have permission to change the role to ADMIN", user.getRole());
            throw new AccessDeniedException("You do not have permission to change the role to ADMIN");
        }

        if (existingUser.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.ADMIN)) {
            log.error("Users with role {} do not have permission to change the role ADMIN to another role", user.getRole());
            throw new AccessDeniedException("You do not have permission to change the role ADMIN to another role");
        }
        if (existingUser.getRole().equals(role)) {
            log.warn("User {} already has role {}", existingUser.getId(), existingUser.getRole());
            throw new IllegalStateException(String.format("This user already has role %s", role));
        }
        existingUser.setRole(role);
        userRepository.save(existingUser);
        log.debug("Role for {} successfully changed", existingUser.getId());
    }

    @Override
    public UserResponseDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        if (existingUser.getIsBlocked() != isBlocked) {
            existingUser.setIsBlocked(isBlocked);
            userRepository.save(existingUser);
            log.debug("Block status {} for {} successfully changed", isBlocked, id);
        } else {
            log.warn("User {} already has block status {}", id, isBlocked);
            throw new IllegalStateException(String.format("User already %s [id=%s]", isBlocked ? "blocked" : "unblocked", id));
        }
        return mapper.toUserDTO(existingUser);
    }

    @Override
    public void blockById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        if (!existingUser.getIsBlocked()) {
            userRepository.blockById(id);
            log.debug("User {} successfully blocked", id);
        } else {
            log.warn("User {} already blocked", id);
            throw new IllegalStateException(String.format("User already blocked [id=%s]", id));
        }
    }

    @Override
    public void unblockById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        if (existingUser.getIsBlocked()) {
            userRepository.unblockById(id);
            log.debug("User {} successfully unblocked", id);
        } else {
            log.warn("User {} already unblocked", id);
            throw new IllegalStateException(String.format("User already unblocked [id=%s]", id));
        }
    }

    @Override
    public void deleteUser(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        if (userRepository.findById(id).isEmpty()) {
            log.warn("Error deleting user {}", id);
            throw new UserNotFoundException("Error deleting user with ID" + id);
        }
        userRepository.deleteById(id);
        log.debug("User {} successfully deleted", id);
    }

    @Override
    public Collection<UserResponseDTO> getAll() {
        log.debug("Method {}", MethodLog.getMethodName());
        Collection<UserResponseDTO> userResponseDTOS = mapper.toUsersDTO(userRepository.findAll());

        if (userResponseDTOS.isEmpty()) {
            log.warn("There is no user in DB");
            throw new UserNotFoundException("There is no user in DB");
        }
        return userResponseDTOS;
    }

    @Override
    public Collection<String> findUsersPhoneNumbers(UUID userId) {
        log.debug("Method {}, UUID {}", MethodLog.getMethodName(), userId);
        if (userRepository.findById(userId).isEmpty()) {
            log.warn("Error getting users {} phones with", userId);
            throw new UserNotFoundException(String.format("Error getting users %s phones", userId));
        }
        return phoneRepository.getNumbersByUserId(userId);
    }

    @Override
    @Transactional
    public void assignAvatarToUser(UUID userId, Avatar avatar) {
        userRepository.setAvatarForUser(avatar, userId);
    }
}
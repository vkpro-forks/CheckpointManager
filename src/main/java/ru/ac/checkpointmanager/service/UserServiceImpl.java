package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.DateOfBirthFormatException;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.Mapper;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.validateDOB;
import static ru.ac.checkpointmanager.utils.Mapper.toUserDTO;
import static ru.ac.checkpointmanager.utils.Mapper.toUsersDTO;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO findById(UUID id) {
        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        return toUserDTO(foundUser);
    }

    @Override
    public List<TerritoryDTO> findTerritoriesByUserId(UUID userId) {
        List<Territory> territories = userRepository.findTerritoriesByUserId(userId);
        if (territories.isEmpty()) {
            throw new TerritoryNotFoundException(String.format("Territory for User not found [user_id=%s]", userId));
        }
        return mapper.toTerritoriesDTO(territories);
    }

    @Override
    public Collection<UserDTO> findByName(String name) {
        Collection<UserDTO> userDTOS = toUsersDTO(userRepository
                .findUserByFullNameContainingIgnoreCase(name));

        if (userDTOS.isEmpty()) {
            throw new UserNotFoundException("There is no user with name containing " + name);
        }
        return userDTOS;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        User foundUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User not found [id=%s]", userDTO.getId())));
        if (!validateDOB(userDTO.getDateOfBirth())) {
            throw new DateOfBirthFormatException("Date of birth should not be greater than the current date");
        }

        if (!findUsersPhoneNumbers(userDTO.getId()).contains(cleanPhone(userDTO.getMainNumber()))) {
            throw new PhoneNumberNotFoundException(String.format
                    ("Phone number %s does not exist", userDTO.getMainNumber()));
        }
        foundUser.setFullName(userDTO.getFullName());
        foundUser.setDateOfBirth(userDTO.getDateOfBirth());
        foundUser.setMainNumber(cleanPhone(userDTO.getMainNumber()));
        foundUser.setEmail(userDTO.getEmail());

        userRepository.save(foundUser);

        return toUserDTO(foundUser);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) { //  Principal представляет собой пользователя, который был идентифицирован в результате процесса аутентификации
        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal(); // получаем юзера из connectedUser, который представляет текущего пользователя

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    @Override
    public void changeRole(UUID id, Role role, Principal connectedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (role == Role.ADMIN && !user.getRole().equals(Role.ADMIN)) { // если НЕадмин хочет назначить кому-то админа
            throw new AccessDeniedException("You do not have permission to change the role to ADMIN");
        }

        if (existingUser.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.ADMIN)) { // если НЕадмин пытается сменить роль админа на любую другую
            throw new AccessDeniedException("You do not have permission to change the role ADMIN to another role");
        }
        if (existingUser.getRole().equals(role)) {
            throw new IllegalStateException(String.format("This user already has role %s", role));
        }
        existingUser.setRole(role);
        userRepository.save(existingUser);
    }

    //    два варианта блокировки пользователя
    //    первый: с помощью одного метода можно и заблокировать и разблокировать по айди
    @Override
    public UserDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));
        if (existingUser.getIsBlocked() != isBlocked) {
            existingUser.setIsBlocked(isBlocked);
            userRepository.save(existingUser);
        } else {
            throw new IllegalStateException(String.format("User already %s [id=%s]", isBlocked ? "blocked" : "unblocked", id));
        }
        return toUserDTO(existingUser);
    }

    //    второй: два разных метода для блокировки или разблокировки по айди,
    //    логика блокировки через sql запрос в репозитории
    @Override
    public void blockById(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        if (!existingUser.getIsBlocked()) {
            userRepository.blockById(id);
        } else {
            throw new IllegalStateException(String.format("User already blocked [id=%s]", id));
        }
    }

    @Override
    public void unblockById(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [id=%s]", id)));
        if (existingUser.getIsBlocked()) {
            userRepository.unblockById(id);
        } else {
            throw new IllegalStateException(String.format("User already unblocked [id=%s]", id));
        }
    }

    @Override
    public void deleteUser(UUID id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException("Error deleting user with ID" + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public Collection<UserDTO> getAll() {
        Collection<UserDTO> userDTOS = toUsersDTO(userRepository.findAll());

        if (userDTOS.isEmpty()) {
            throw new UserNotFoundException("There is no user in DB");
        }
        return userDTOS;
    }

    @Override
    public Collection<String> findUsersPhoneNumbers(UUID userId) {
        return phoneRepository.getNumbersByUserId(userId);
    }
}

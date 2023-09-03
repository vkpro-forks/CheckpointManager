package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.DateOfBirthFormatException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (!validateDOB(userDTO.getDateOfBirth())) {
            throw new DateOfBirthFormatException
                    ("Date of birth should not be greater than the current date " +
                            "or less than 100 years from the current moment");
        }

        User user = convertToUser(userDTO);
        user.setIsBlocked(false);
        userRepository.save(user);

        return convertToUserDTO(user);
    }

    @Override
    public UserDTO findById(UUID id) {
        return convertToUserDTO(userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by this id does not exist")));
    }

    @Override
    public Collection<UserDTO> findByName(String name) {
        Collection<UserDTO> userDTOS = userRepository.findUserByFullNameContainingIgnoreCase(name).stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());

        if (userDTOS.isEmpty()) {
            throw new UserNotFoundException("There is no user with name containing " + name);
        }
        return userDTOS;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        try {
            User foundUser = userRepository.findById(userDTO.getId())
                    .orElseThrow(UserNotFoundException::new);

            foundUser.setFullName(userDTO.getFullName());
            foundUser.setDateOfBirth(userDTO.getDateOfBirth());
            foundUser.setEmail(userDTO.getEmail());
            foundUser.setPassword(userDTO.getPassword());
            userRepository.save(foundUser);

            return convertToUserDTO(foundUser);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error updating user with ID " + userDTO.getId(), e);
        }
    }

    //    два варианта блокировки пользователя
//    первый: с помощью одного метода можно и заблокировать и разблокировать по айди
    @Override
    public UserDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        try {
            User existingUser = userRepository.findById(id).orElseThrow(
                    UserNotFoundException::new);

            existingUser.setIsBlocked(isBlocked);
            userRepository.save(existingUser);

            return convertToUserDTO(existingUser);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error updating user with ID " + id, e);
        }
    }

    //    второй: два разных метода для блокировки или разблокировки по айди,
//    логика блокировки через sql запрос в репозитории
    @Override
    public void blockById(UUID id) {
        try {
            userRepository.findById(id).orElseThrow(
                    UserNotFoundException::new);

            userRepository.blockById(id);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error updating user with ID " + id, e);
        }
    }

    @Override
    public void unblockById(UUID id) {
        try {
            userRepository.findById(id).orElseThrow(
                    UserNotFoundException::new);

            userRepository.unblockById(id);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error updating user with ID " + id, e);
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
        Collection<UserDTO> userDTOS = userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());

        if (userDTOS.isEmpty()) {
            throw new UserNotFoundException("There is no user in DB");
        }
        return userDTOS;
    }

    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    private ru.ac.checkpointmanager.dto.UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    private Boolean validateDOB(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        if (dateOfBirth.isAfter(currentDate)) {
            return false; // Date of birth is in the future
        }

        Period age = Period.between(dateOfBirth, currentDate);
        return age.getYears() <= 100; // Date of birth is at least 100 years ago
    }
}

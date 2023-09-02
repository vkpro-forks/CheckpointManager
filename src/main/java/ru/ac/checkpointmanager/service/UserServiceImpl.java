package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.UserRepository;

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
        userRepository.save(convertToUser(userDTO));
        return userDTO;
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

    private User convertToUser(ru.ac.checkpointmanager.dto.UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    private ru.ac.checkpointmanager.dto.UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, ru.ac.checkpointmanager.dto.UserDTO.class);
    }
}

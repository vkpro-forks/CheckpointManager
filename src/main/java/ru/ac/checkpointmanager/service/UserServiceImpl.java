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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by this id does not exist"));
    }

    @Override
    public Collection<User> findByName(String name) {
        return userRepository.findUserByFullNameContainingIgnoreCase(name);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId()).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            existingUser.setFullName(user.getFullName());
            existingUser.setDateOfBirth(user.getDateOfBirth());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());

            return userRepository.save(existingUser);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error updating user with ID " + user.getId(), e);
        }
    }

//    два варианта блокировки пользователя
//    первый: с помощью одного метода можно и заблокировать и разблокировать по айди
    @Override
    public User updateBlockStatus(UUID id, Boolean isBlocked) {
        try {
            User existingUser = userRepository.findById(id).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            existingUser.setIsBlocked(isBlocked);
            return userRepository.save(existingUser);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user with ID " + id, e);
        }
    }

//    второй: два разных метода для блокировки или разблокировки по айди,
//    логика блокировки через sql запрос в репозитории
    @Override
    public void blockById(UUID id) {
        try {
            userRepository.findById(id).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            userRepository.blockById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user with ID " + id, e);
        }
    }

    @Override
    public void unblockById(UUID id) {
        try {
            userRepository.findById(id).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            userRepository.unblockById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user with ID " + id, e);
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
    public Collection<User> getAll() {
        Collection<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            throw new UserNotFoundException("There is no user in DB");
        }
        return users;
    }

    @Override
    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    @Override
    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}

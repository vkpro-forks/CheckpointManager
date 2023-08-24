package ru.ac.checkpointmanager.service;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.util.Collection;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
    public User updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId()).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            existingUser.setFullName(user.getFullName());
            existingUser.setDateOfBirth(user.getDateOfBirth());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());

            return userRepository.save(existingUser);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user with ID " + user.getId());
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
}

package ru.ac.checkpointmanager.service;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        /* проверка, на уже зарегестрированного пользователя, по номеру телефона

        if (checkPhoneNumber(user.getPhoneNumber) == true) {
            throw new PhoneAlreadyExistException();
        }  */
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User by this id does not exist"));
    }

    @Override
    public Collection<User> findByName(String name) {
        return userRepository.findUserByFullNameContainingIgnoreCase(name);
    }
}

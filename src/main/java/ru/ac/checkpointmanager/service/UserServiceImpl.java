package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.dto.UserPhoneDTO;
import ru.ac.checkpointmanager.exception.DateOfBirthFormatException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.util.Collection;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.validateDOB;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PhoneService phoneService;
    private final ModelMapper modelMapper;


    @Transactional
    @Override
    public UserPhoneDTO createUser(UserPhoneDTO userPhoneDTO) {
        // проверяем дату
        if (!validateDOB(userPhoneDTO.getUserDTO().getDateOfBirth())) {
            throw new DateOfBirthFormatException
                    ("Date of birth should not be greater than the current date " +
                            "or less than 100 years from the current moment");
        }

        PhoneDTO phoneDTO = userPhoneDTO.getPhoneDTO();

        // проверяем существует ли номер телефона по номеру (по id нет смысла, тк его вводить при создании необязательно)
        if (phoneRepository.existsByNumber(phoneDTO.getNumber())) {
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number [number=%s] already exist", phoneDTO.getNumber()));
        }

        // сохраняем юзера в БД, присваиваем основной номер, ставим незаблокированным
        User user = convertToUser(userPhoneDTO.getUserDTO());
        user.setMainNumber(cleanPhone(phoneDTO.getNumber()));
        user.setIsBlocked(false);
        userRepository.save(user);

        // устанавливаем идентификатор пользователя для PhoneDTO и создаем номер телефона,
        // метод из PhoneService сохранит его в бд
        phoneDTO.setUserId(user.getId());
        PhoneDTO savedPhone = phoneService.createPhoneNumber(phoneDTO);

        // возращаем юзера и номер с установленным id
        return convertToUserPhoneDTO(user, savedPhone);
    }

    @Override
    public UserDTO findById(UUID id) {
        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));
        return convertToUserDTO(foundUser);
    }

    @Override
    public Collection<UserDTO> findByName(String name) {
        Collection<UserDTO> userDTOS = userRepository.findUserByFullNameContainingIgnoreCase(name).stream()
                .map(this::convertToUserDTO)
                .collect(toList());

        if (userDTOS.isEmpty()) {
            throw new UserNotFoundException("There is no user with name containing " + name);
        }
        return userDTOS;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        User foundUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User not found [Id=%s]", userDTO.getId())));

        if (!validateDOB(userDTO.getDateOfBirth())) {
            throw new DateOfBirthFormatException("Date of birth should not be greater than the current date " +
                    "or less than 100 years from the current moment");
        }

        // проверяем, регистрировал ли на себя юзер введеный номер
        if (!findUsersPhoneNumbers(userDTO.getId()).contains(userDTO.getMainNumber())) {
            throw new PhoneNumberNotFoundException(String.format
                    ("Phone number [number=%s] does not exist", userDTO.getMainNumber()));
        }

        foundUser.setFullName(userDTO.getFullName());
        foundUser.setDateOfBirth(userDTO.getDateOfBirth());

        // меняем основной номер из списка номеров юзера
        foundUser.setMainNumber(cleanPhone(userDTO.getMainNumber()));

        foundUser.setEmail(userDTO.getEmail());
        foundUser.setPassword(userDTO.getPassword());

        userRepository.save(foundUser);

        return convertToUserDTO(foundUser);
    }

    //    два варианта блокировки пользователя

    //    первый: с помощью одного метода можно и заблокировать и разблокировать по айди
    @Override
    public UserDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        existingUser.setIsBlocked(isBlocked);
        userRepository.save(existingUser);

        return convertToUserDTO(existingUser);
    }

    //    второй: два разных метода для блокировки или разблокировки по айди,
    //    логика блокировки через sql запрос в репозитории
    @Override
    public void blockById(UUID id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        userRepository.blockById(id);
    }

    @Override
    public void unblockById(UUID id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));

        userRepository.unblockById(id);
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
                .collect(toList());

        if (userDTOS.isEmpty()) {
            throw new UserNotFoundException("There is no user in DB");
        }
        return userDTOS;
    }

    // метод ищет в таблице phones номера, которые привязаны к переданному user_id
    @Override
    public Collection<String> findUsersPhoneNumbers(UUID userId) {
        return phoneRepository.getNumbersByUserId(userId);
    }

    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    private UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    private UserPhoneDTO convertToUserPhoneDTO(User user, PhoneDTO phoneDTO) {
        UserPhoneDTO userPhoneDTO = new UserPhoneDTO();
        userPhoneDTO.setUserDTO(modelMapper.map(user, UserDTO.class));
        userPhoneDTO.setPhoneDTO(phoneDTO);
        return userPhoneDTO;
    }
}

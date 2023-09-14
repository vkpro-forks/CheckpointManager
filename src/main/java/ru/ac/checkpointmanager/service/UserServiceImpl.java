package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.*;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.Mapper;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.model.enums.PhoneNumberType.MOBILE;
import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.validateDOB;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PhoneService phoneService;
    private final Mapper mapper;

    private final Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

    @Transactional
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // проверяем дату
        if (!validateDOB(userDTO.getDateOfBirth())) {
            throw new DateOfBirthFormatException
                    ("Date of birth should not be greater than the current date " +
                            "or less than 100 years from the current moment");
        }
        // проверяем существует ли номер телефона по номеру
        if (phoneRepository.existsByNumber(cleanPhone(userDTO.getMainNumber()))) {
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number [number=%s] already exist", userDTO.getMainNumber()));
        }

        // сохраняем юзера в БД, присваиваем основной номер, ставим незаблокированным
        User user = mapper.toUser(userDTO);
        user.setMainNumber(cleanPhone(userDTO.getMainNumber()));
        user.setIsBlocked(false);
        user.setAddedAt(currentTimestamp);
        userRepository.save(user);

        // метод из PhoneService сохранит его в бд
        PhoneDTO phoneDTO = createPhoneDTO(user);
        phoneService.createPhoneNumber(phoneDTO);

        return mapper.toUserDTO(user);
    }

    // устанавливаем поля для сущности PhoneDTO из данных сохранённого юзера, тип телефона по умолчанию мобильный
    private PhoneDTO createPhoneDTO(User user) {
        PhoneDTO phoneDTO = new PhoneDTO();
        phoneDTO.setUserId(user.getId());
        phoneDTO.setNumber(user.getMainNumber());
        phoneDTO.setType(MOBILE);
        return phoneDTO;
    }

    @Override
    public UserDTO findById(UUID id) {
        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));
        return mapper.toUserDTO(foundUser);
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
        Collection<UserDTO> userDTOS = mapper.toUsersDTO(userRepository
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
                        String.format("User not found [Id=%s]", userDTO.getId())));
        if (!validateDOB(userDTO.getDateOfBirth())) {
            throw new DateOfBirthFormatException("Date of birth should not be greater than the current date " +
                    "or less than 100 years from the current moment");
        }
        // проверяем, регистрировал ли на себя юзер введеный номер
        if (!findUsersPhoneNumbers(userDTO.getId()).contains(cleanPhone(userDTO.getMainNumber()))) {
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

        return mapper.toUserDTO(foundUser);
    }

    //    два варианта блокировки пользователя
    //    первый: с помощью одного метода можно и заблокировать и разблокировать по айди
    @Override
    public UserDTO updateBlockStatus(UUID id, Boolean isBlocked) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User not found [Id=%s]", id)));
        existingUser.setIsBlocked(isBlocked);
        userRepository.save(existingUser);

        return mapper.toUserDTO(existingUser);
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
        Collection<UserDTO> userDTOS = mapper.toUsersDTO(userRepository.findAll());

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
}

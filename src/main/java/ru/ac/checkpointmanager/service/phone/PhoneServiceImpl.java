package ru.ac.checkpointmanager.service.phone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.PhoneMapper;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.Collection;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneServiceImpl implements PhoneService {
    private final UserRepository userRepository;
    private static final String PHONE_NUMBER_NOT_FOUND_MSG = "Phone number with id: %s doesn't exist";
    private static final String PHONE_NUMBER_NOT_FOUND_LOG = "Phone number with [id: {}] doesn't exist";
    private static final String METHOD_CALLED = "Method {}";
    private final PhoneRepository phoneRepository;
    private final PhoneMapper phoneMapper;

    @Override
    @Transactional
    public PhoneDTO createPhoneNumber(PhoneDTO phoneDTO) {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        String cleanedPhone = cleanPhone(phoneDTO.getNumber());

        User user = userRepository.findById(phoneDTO.getUserId()).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(phoneDTO.getUserId()));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(phoneDTO.getUserId()));
                }
        );
        if (phoneRepository.existsByNumber(cleanedPhone)) {
            String phoneNumber = phoneDTO.getNumber();
            log.warn(ExceptionUtils.PHONE_EXISTS.formatted(phoneNumber));
            throw new PhoneAlreadyExistException(ExceptionUtils.PHONE_EXISTS.formatted(phoneNumber));
        }
        Phone phone = phoneMapper.toPhone(phoneDTO);
        phone.setUser(user);
        phone.setNumber(cleanedPhone);
        Phone savedPhone = phoneRepository.save(phone);

        log.info("Phone {} saved", phone.getNumber());
        return phoneMapper.toPhoneDTO(savedPhone);
    }

    @Cacheable(value = "phone", key = "#id")
    @Override
    @Transactional(readOnly = true)
    public PhoneDTO findById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        return phoneMapper.toPhoneDTO(findPhoneById(id));
    }

    @Override
    public Phone findPhoneById(UUID id) {
        return phoneRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(PHONE_NUMBER_NOT_FOUND_LOG, id);
                    return new PhoneNumberNotFoundException(PHONE_NUMBER_NOT_FOUND_MSG.formatted(id));
                });
    }

    @CacheEvict(value = "phone", key = "#phoneDTO.id")
    @Override
    @Transactional
    public PhoneDTO updatePhoneNumber(PhoneDTO phoneDTO) {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        UUID phoneId = phoneDTO.getId();
        Phone foundPhone = phoneRepository.findById(phoneId).orElseThrow(
                () -> {
                    log.warn(PHONE_NUMBER_NOT_FOUND_LOG, phoneId);
                    return new PhoneNumberNotFoundException(PHONE_NUMBER_NOT_FOUND_MSG.formatted(phoneId));
                });
        if (phoneRepository.existsByNumber(cleanPhone(phoneDTO.getNumber()))) {
            log.warn("Phone {} already taken", phoneDTO.getNumber());
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", phoneDTO.getNumber()));
        }
        foundPhone.setNumber(cleanPhone(phoneDTO.getNumber()));
        foundPhone.setType(phoneDTO.getType());
        foundPhone.setNote(phoneDTO.getNote());

        phoneRepository.save(foundPhone);
        log.info("Phone {} saved", foundPhone.getNumber());
        return phoneMapper.toPhoneDTO(foundPhone);
    }

    @CacheEvict(value = "phone", key = "#id")
    @Override
    @Transactional
    public void deletePhoneNumber(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        if (phoneRepository.findById(id).isEmpty()) {
            log.warn(PHONE_NUMBER_NOT_FOUND_LOG, id);
            throw new PhoneNumberNotFoundException(PHONE_NUMBER_NOT_FOUND_MSG.formatted(id));
        }
        phoneRepository.deleteById(id);
        log.info("Phone with [id: {}] was successfully deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<PhoneDTO> getAll() {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        return phoneMapper.toPhonesDTO(phoneRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByNumber(String number) {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        return phoneRepository.existsByNumber(number);
    }


}
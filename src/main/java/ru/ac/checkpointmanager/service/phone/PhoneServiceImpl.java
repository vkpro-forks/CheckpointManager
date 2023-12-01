package ru.ac.checkpointmanager.service.phone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.InvalidPhoneNumberException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.mapper.PhoneMapper;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.Collection;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.isValidPhoneNumber;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneServiceImpl implements PhoneService {
    private static final String PHONE_NUMBER_NOT_FOUND_MSG = "Phone number with id: %s doesn't exist";
    private static final String PHONE_NUMBER_NOT_FOUND_LOG = "Phone number with [id: {}] doesn't exist";
    private static final String METHOD_CALLED = "Method {}";
    private final PhoneRepository phoneRepository;
    private final PhoneMapper phoneMapper;

    @Override
    @Transactional
    public PhoneDTO createPhoneNumber(PhoneDTO phoneDTO) {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());

        if (!isValidPhoneNumber(phoneDTO.getNumber())) {
            log.warn("Phone number {} contains invalid characters", phoneDTO.getNumber());
            throw new InvalidPhoneNumberException(String.format("Phone number %s contains invalid characters", phoneDTO.getNumber()));
        }
        phoneDTO.setNumber(cleanPhone(phoneDTO.getNumber()));

        if (phoneRepository.existsByNumber(phoneDTO.getNumber())) {
            log.warn("Phone {} already exist or NULL", phoneDTO.getNumber());
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", phoneDTO.getNumber()));
        }
        Phone phone = phoneRepository.save(phoneMapper.toPhone(phoneDTO));
        log.info("Phone {} saved", phone.getNumber());
        return phoneMapper.toPhoneDTO(phone);
    }

    @Override
    public PhoneDTO findById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        Phone foundPhone = phoneRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(PHONE_NUMBER_NOT_FOUND_LOG, id);
                    return new PhoneNumberNotFoundException(PHONE_NUMBER_NOT_FOUND_MSG.formatted(id));
                });
        return phoneMapper.toPhoneDTO(foundPhone);
    }

    @Override
    public PhoneDTO updatePhoneNumber(PhoneDTO phoneDTO) {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        UUID phoneId = phoneDTO.getId();
        Phone foundPhone = phoneRepository.findById(phoneId).orElseThrow(
                () -> {
                    log.warn(PHONE_NUMBER_NOT_FOUND_LOG, phoneId);
                    return new PhoneNumberNotFoundException(PHONE_NUMBER_NOT_FOUND_MSG.formatted(phoneId));
                });
        if (phoneRepository.existsByNumber(phoneDTO.getNumber())) {
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


    @Override
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
    public Collection<PhoneDTO> getAll() {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        return phoneMapper.toPhonesDTO(phoneRepository.findAll());
    }

    @Override
    public Boolean existsByNumber(String number) {
        log.debug(METHOD_CALLED, MethodLog.getMethodName());
        return phoneRepository.existsByNumber(number);
    }
}
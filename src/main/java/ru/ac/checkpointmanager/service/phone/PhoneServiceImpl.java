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

    private final PhoneRepository phoneRepository;
    private final PhoneMapper phoneMapper;

    @Override
    @Transactional
    public PhoneDTO createPhoneNumber(PhoneDTO phoneDTO) {
        log.info("Method {}", MethodLog.getMethodName());

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
        log.debug("Phone {} saved", phone.getNumber());
        return phoneMapper.toPhoneDTO(phone);
    }

    @Override
    public PhoneDTO findById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        Phone foundPhone = phoneRepository.findById(id).orElseThrow(
                () -> new PhoneNumberNotFoundException("The number by this id does not exist"));
        return phoneMapper.toPhoneDTO(foundPhone);
    }

    @Override
    @Transactional
    public PhoneDTO updatePhoneNumber(PhoneDTO phoneDTO) {
        log.debug("Method {}", MethodLog.getMethodName());
        Phone foundPhone = phoneRepository.findById(phoneDTO.getId()).orElseThrow(
                () -> new PhoneNumberNotFoundException("The number by this id does not exist"));

        if (phoneRepository.existsByNumber(phoneDTO.getNumber())) {
            log.warn("Phone {} already taken", phoneDTO.getNumber());
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", phoneDTO.getNumber()));
        }
        foundPhone.setNumber(cleanPhone(phoneDTO.getNumber()));
        foundPhone.setType(phoneDTO.getType());
        foundPhone.setNote(phoneDTO.getNote());

        phoneRepository.save(foundPhone);
        log.debug("Phone {} saved", foundPhone.getNumber());
        return phoneMapper.toPhoneDTO(foundPhone);
    }


    @Override
    public void deletePhoneNumber(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        if (phoneRepository.findById(id).isEmpty()) {
            log.warn("Error deleting phone {}", id);
            throw new PhoneNumberNotFoundException("Error deleting phone number with ID" + id);
        }
        phoneRepository.deleteById(id);
    }

    @Override
    public Collection<PhoneDTO> getAll() {
        log.debug("Method {}", MethodLog.getMethodName());
        Collection<PhoneDTO> numbers = phoneMapper.toPhonesDTO(phoneRepository.findAll());

        if (numbers.isEmpty()) {
            log.warn("There is no phone in DB");
            throw new PhoneNumberNotFoundException("There is no phone number in DB");
        }
        return numbers;
    }

    @Override
    public Boolean existsByNumber(String number) {
        log.debug("Method {}", MethodLog.getMethodName());
        return phoneRepository.existsByNumber(number);
    }
}
package ru.ac.checkpointmanager.service.phone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.InvalidPhoneNumberException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.Collection;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;
import static ru.ac.checkpointmanager.utils.FieldsValidation.isValidPhoneNumber;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneServiceImpl implements PhoneService {

    private final PhoneRepository phoneRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public PhoneDTO createPhoneNumber(PhoneDTO phoneDTO) {
        log.info("method createPhoneNumber was invoked");

        if (!isValidPhoneNumber(phoneDTO.getNumber())) {
            log.warn("Invalid phone number");
            throw new InvalidPhoneNumberException(String.format("Phone number %s contains invalid characters", phoneDTO.getNumber()));
        }
        phoneDTO.setNumber(cleanPhone(phoneDTO.getNumber()));

        if (phoneRepository.existsByNumber(phoneDTO.getNumber())) {
            log.error("phone already exist or NULL");
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", phoneDTO.getNumber()));
        }
        Phone phone = phoneRepository.save(mapper.toPhone(phoneDTO));
        return mapper.toPhoneDTO(phone);
    }

    @Override
    public PhoneDTO findById(UUID id) {
        Phone foundPhone = phoneRepository.findById(id).orElseThrow(
                () -> new PhoneNumberNotFoundException("The number by this id does not exist"));
        return mapper.toPhoneDTO(foundPhone);
    }

    @Override
    public PhoneDTO updatePhoneNumber(PhoneDTO phoneDTO) {
        Phone foundPhone = phoneRepository.findById(phoneDTO.getId()).orElseThrow(
                () -> new PhoneNumberNotFoundException("The number by this id does not exist"));

        if (phoneRepository.existsByNumber(phoneDTO.getNumber())) {
            throw new PhoneAlreadyExistException(String.format
                    ("Phone number %s already exist", phoneDTO.getNumber()));
        }
        foundPhone.setNumber(cleanPhone(phoneDTO.getNumber()));
        foundPhone.setType(phoneDTO.getType());
        foundPhone.setNote(phoneDTO.getNote());

        phoneRepository.save(foundPhone);

        return mapper.toPhoneDTO(foundPhone);
    }


    @Override
    public void deletePhoneNumber(UUID id) {
        if (phoneRepository.findById(id).isEmpty()) {
            throw new PhoneNumberNotFoundException("Error deleting phone number with ID" + id);
        }
        phoneRepository.deleteById(id);
    }

    @Override
    public Collection<PhoneDTO> getAll() {
        Collection<PhoneDTO> numbers = mapper.toPhonesDTO(phoneRepository.findAll());

        if (numbers.isEmpty()) {
            throw new PhoneNumberNotFoundException("There is no phone number in DB");
        }
        return numbers;
    }

    @Override
    public Boolean existsByNumber(String number) {
        return phoneRepository.existsByNumber(number);
    }
}
package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.Collection;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.FieldsValidation.cleanPhone;

@Service
@RequiredArgsConstructor
public class PhoneServiceImpl implements PhoneService {

    private final PhoneRepository phoneRepository;
    private final Mapper mapper;

    @Override
    public PhoneDTO createPhoneNumber(PhoneDTO phoneDTO) {
        phoneDTO.setNumber(cleanPhone(phoneDTO.getNumber()));
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
}
package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.repository.PhoneRepository;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhoneServiceImpl implements PhoneService {

    private final PhoneRepository phoneRepository;
    private final ModelMapper modelMapper;

    @Override
    public PhoneDTO createPhoneNumber(PhoneDTO phoneDTO) {
        phoneDTO.setNumber(cleanPhone(phoneDTO.getNumber()));
        phoneRepository.save(convertToPhone(phoneDTO));
        return phoneDTO;
    }

    @Override
    public PhoneDTO findById(UUID id) {
        return convertToPhoneDTO(phoneRepository.findById(id).orElseThrow(
                () -> new PhoneNumberNotFoundException("The number by this id does not exist")));
    }

    @Override
    public PhoneDTO updatePhoneNumber(PhoneDTO phoneDTO) {
        try {
            Phone foundPhone = phoneRepository.findById(phoneDTO.getId())
                    .orElseThrow(PhoneNumberNotFoundException::new);

            foundPhone.setNumber(phoneDTO.getNumber());
            foundPhone.setType(phoneDTO.getType());
            foundPhone.setNote(phoneDTO.getNote());

            return convertToPhoneDTO(foundPhone);
        } catch (PhoneNumberNotFoundException e) {
            throw new PhoneNumberNotFoundException("Error updating user with ID " + phoneDTO.getId(), e);
        }
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
        Collection<PhoneDTO> numbers = phoneRepository.findAll().stream()
                .map(this::convertToPhoneDTO)
                .collect(Collectors.toList());

        if (numbers.isEmpty()) {
            throw new PhoneNumberNotFoundException("There is no phone number in DB");
        }
        return numbers;
    }

    private Phone convertToPhone(PhoneDTO phoneDTO) {
        return modelMapper.map(phoneDTO, Phone.class);
    }

    private PhoneDTO convertToPhoneDTO(Phone phone) {
        return modelMapper.map(phone, PhoneDTO.class);
    }

    private String cleanPhone(String phone) {
        // Удаление всех символов, кроме цифр
        String cleanedPhone = phone.replaceAll("[^\\d]", "");

        // Удаление ведущих нулей
        cleanedPhone = cleanedPhone.replaceFirst("^0+(?!$)", "");

        return cleanedPhone;
    }
}


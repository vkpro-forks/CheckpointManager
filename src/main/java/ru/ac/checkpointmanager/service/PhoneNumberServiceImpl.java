package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.PhoneNumberDTO;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.PhoneNumber;
import ru.ac.checkpointmanager.repository.PhoneNumberRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneNumberServiceImpl implements PhoneNumberService {

    private final PhoneNumberRepository numberRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public PhoneNumber createPhoneNumber(PhoneNumber phoneNumber) {
        try {  /* нельзя же просто номер в базу запихнуть, только к существующему юзеру привязать */
            userRepository.findById(phoneNumber.getUser().getId()).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            return numberRepository.save(phoneNumber);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error connecting number with user id"
                    + phoneNumber.getUser().getId(), e);
        }
    }

    @Override
    public PhoneNumber findById(UUID id) {
        return numberRepository.findById(id).orElseThrow(
                () -> new PhoneNumberNotFoundException("The number by this id does not exist"));
    }

    @Override
    public PhoneNumber updatePhoneNumber(PhoneNumber phoneNumber) {
        try {
            numberRepository.findById(phoneNumber.getId()).orElseThrow(
                    () -> new PhoneNumberNotFoundException("The number by this id does not exist"));
            /* на случай, еасли захотят id юзера изменить */
            userRepository.findById(phoneNumber.getUser().getId()).orElseThrow(
                    () -> new UserNotFoundException("User by this id does not exist"));

            return numberRepository.save(phoneNumber);

        } catch (PhoneNumberNotFoundException e) {
            throw new PhoneNumberNotFoundException("Error updating user with ID " + phoneNumber.getId(), e);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Error connecting number with user id"
                    + phoneNumber.getUser().getId(), e);
        }
    }

    @Override
    public void deletePhoneNumber(UUID id) {
        if (numberRepository.findById(id).isEmpty()) {
            throw new PhoneNumberNotFoundException("Error deleting phone number with ID" + id);
        }
        numberRepository.deleteById(id);
    }

    @Override
    public Collection<PhoneNumber> getAll() {
        Collection<PhoneNumber> numbers = numberRepository.findAll();
        if (numbers.isEmpty()) {
            throw new PhoneNumberNotFoundException("There is no phone number in DB");
        }
        return numbers;
    }

    @Override
    public PhoneNumber convertToPhoneNumber(PhoneNumberDTO phoneNumberDTO) {
        return modelMapper.map(phoneNumberDTO, PhoneNumber.class);
    }

    @Override
    public PhoneNumberDTO convertToPhoneNumberDTO(PhoneNumber phoneNumber) {
        return modelMapper.map(phoneNumber, PhoneNumberDTO.class);
    }
}

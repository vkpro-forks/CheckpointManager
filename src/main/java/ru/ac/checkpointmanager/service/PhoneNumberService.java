package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.dto.PhoneNumberDTO;
import ru.ac.checkpointmanager.model.PhoneNumber;

import java.util.Collection;
import java.util.UUID;

public interface PhoneNumberService {

    PhoneNumber createPhoneNumber(PhoneNumber phoneNumber);

    PhoneNumber findById(UUID id);

    PhoneNumber updatePhoneNumber(PhoneNumber phoneNumber);

    void deletePhoneNumber(UUID id);

    Collection<PhoneNumber> getAll();

    PhoneNumber convertToPhoneNumber(PhoneNumberDTO phoneNumberDTO);

    PhoneNumberDTO convertToPhoneNumberDTO(PhoneNumber phoneNumber);
}

package ru.ac.checkpointmanager.service.phone;

import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.model.Phone;

import java.util.Collection;
import java.util.UUID;

public interface PhoneService {

    PhoneDTO createPhoneNumber(PhoneDTO phoneDTO);

    PhoneDTO findById(UUID id);

    Phone findPhoneById(UUID id);

    PhoneDTO updatePhoneNumber(PhoneDTO phone);

    void deletePhoneNumber(UUID id);

    Collection<PhoneDTO> getAll();

    Boolean existsByNumber(String number);
}

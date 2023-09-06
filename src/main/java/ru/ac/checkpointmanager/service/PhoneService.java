package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.dto.PhoneDTO;

import java.util.Collection;
import java.util.UUID;

public interface PhoneService {

    PhoneDTO createPhoneNumber(PhoneDTO phoneDTO);

    PhoneDTO findById(UUID id);

    PhoneDTO updatePhoneNumber(PhoneDTO phone);

    void deletePhoneNumber(UUID id);

    Collection<PhoneDTO> getAll();
}

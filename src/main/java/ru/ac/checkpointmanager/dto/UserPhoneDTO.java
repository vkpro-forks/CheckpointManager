package ru.ac.checkpointmanager.dto;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class UserPhoneDTO {

    @Valid
    private UserDTO userDTO;

    @Valid
    private PhoneDTO phoneDTO;
}

package ru.ac.checkpointmanager.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserFilterParams {

    private String territories;

    private String role;

    private Boolean isBlocked;
}

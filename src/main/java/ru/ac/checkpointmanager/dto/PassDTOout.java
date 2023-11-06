package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassDTOout {

    private UUID id;

    private UserDTO user;

    String name;

    private String statusDescription;

    private String typeTimeDescription;

    private TerritoryDTO territory;

    private String note;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String dtype;

    private PersonDTO person;

    private CarDTO car;

}
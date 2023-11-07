package ru.ac.checkpointmanager.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.UUID;

@Data
public class CarDTO {


    private UUID id;

    @NotNull
    private String licensePlate;

    private CarBrand brand;
}

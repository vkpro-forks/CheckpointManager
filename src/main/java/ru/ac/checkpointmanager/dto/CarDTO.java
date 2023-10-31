package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.UUID;

@Data
public class CarDTO {

    private UUID uuid;

    @NotNull
    private String licensePlate;

    private CarBrand brand;
}

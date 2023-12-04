package ru.ac.checkpointmanager.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarDTO {

    private UUID id;

    @NotNull
    @Size(min = 6, max = 10, message = "The number must be at least 6 characters and no more than 10")
    @Pattern(regexp = "^[АВЕКМНОРСТУХавекмнорстухA-Za-z0-9]+$", message = "Invalid characters in license plate")
    private String licensePlate;

    private CarBrand brand;
}

package ru.ac.checkpointmanager.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.UUID;

@Data
public class CarDTO {


    private UUID id;

    @NotNull
    @Column(name = "license_plate")
    @Size(min = 6, max = 10, message = "The number must be at least 6 characters and no more than 10")
    @Pattern(regexp = "^[АВЕКМНОРСТУХA-Z0-9]+$", message = "Invalid characters in license plate")
    private String licensePlate;

    private CarBrand brand;
}

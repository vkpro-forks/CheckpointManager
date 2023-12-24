package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarBrandDTO {

    @NotNull
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s-]+$", message = "Brand name should contain only letters, spaces, numbers, and hyphens.")
    @Size(min = 2, max = 25, message = "Brand name must be less than 25 characters!")
    private String brand;

}

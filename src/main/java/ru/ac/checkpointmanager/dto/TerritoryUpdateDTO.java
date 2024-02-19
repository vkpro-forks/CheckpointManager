package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TerritoryUpdateDTO {

    @NotNull
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 60)
    @Pattern(regexp = "^(?=.*[a-zA-Zа-яА-Я]).*$",
            message = "Name must contain at least one letter")
    private String name;

    @Size(max = 200)
    private String note;

    @NotBlank
    @Size(min = 2, max = 30)
    private String city;

    @NotBlank
    @Size(min = 2, max = 60)
    private String address;
}

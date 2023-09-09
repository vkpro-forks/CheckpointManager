package ru.ac.checkpointmanager.model.car;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Entity
@Table(name = "trailer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "license_plate")
    @Size(min = 6, max = 10)
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9]+$", message = "Invalid characters in license plate")
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private CarColor color;

}

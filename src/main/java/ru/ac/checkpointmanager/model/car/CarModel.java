package ru.ac.checkpointmanager.model.car;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "car_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private CarBrand brand;

    @NotNull
    @Column(unique = true)
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s-]+$", message = "Model name should contain only letters, numbers, spaces, and dashes!")
    @Size(max = 25, message = "Model name must be less than 25 characters!")
    private String model;

}






package ru.ac.checkpointmanager.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "license_plate")
    private String licensePlate;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private CarBrand brand;

    @Column(name = "model")
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CarType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private CarColor color;

    @Column(name = "year")
    private int year;

}

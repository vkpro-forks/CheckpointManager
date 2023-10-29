package ru.ac.checkpointmanager.model.car;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.CarColor;
import ru.ac.checkpointmanager.model.enums.CarType;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cars")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "license_plate")
    @Size(min = 6, max = 10, message = "The number must be at least 6 characters and no more than 10")
    @Pattern(regexp = "^[АВЕКМНОРСТУХA-Z0-9]+$", message = "Invalid characters in license plate")
    private String licensePlate;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private CarBrand brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CarType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private CarColor color;

    @Column(name = "year")
    private Integer year;

    @OneToMany(mappedBy = "car")
    private List<PassAuto> passes;

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate.toUpperCase();
    }
}

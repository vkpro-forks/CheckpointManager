package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Size(max = 25, message = "Имя модели должно быть не более 25 символов")
    private String model;

    @PrePersist
    @PreUpdate
    public void toProperName() {
        if (model != null) {
            model = model.substring(0, 1).toUpperCase() + model.substring(1).toLowerCase();
        }
    }
}






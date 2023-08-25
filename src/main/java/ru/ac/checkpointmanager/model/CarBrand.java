package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car_brand")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "brand")
    @NotNull
    @Size(min = 2, max = 25, message = "Имя бренда должно быть не более 25 символов")
    @Pattern(regexp = "^[^0-9]*$", message = "Имя бренда не должно содержать цифр")
    private String brand;

//    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
//    private List<Car> cars = new ArrayList<>();
//
//    @OneToMany(mappedBy = "brand")
//    private List<CarModel> models = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void toProperName() {
        if (brand != null) {
            brand = brand.substring(0, 1).toUpperCase() + brand.substring(1).toLowerCase();
        }
    }
}

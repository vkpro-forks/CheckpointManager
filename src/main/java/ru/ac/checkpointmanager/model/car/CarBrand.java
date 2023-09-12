package ru.ac.checkpointmanager.model.car;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s-]+$", message = "Brand name should contain only letters, spaces, numbers, and hyphens.")
    @Size(min = 2, max = 25, message = "Brand name must be less than 25 characters!")
    private String brand;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "brand")
    private List<CarModel> models = new ArrayList<>();

}

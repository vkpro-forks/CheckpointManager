package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.car.Car;


@Entity
@Setter
@Getter
@DiscriminatorValue("AUTO")
public class PassAuto extends Pass {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "car_id")
    private Car car;
}
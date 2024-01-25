package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.car.Car;

@StaticMetamodel(Car.class)
public abstract class Car_ {

    public static final String LICENSE_PLATE = "licensePlate";

    private Car_() {
    }

}

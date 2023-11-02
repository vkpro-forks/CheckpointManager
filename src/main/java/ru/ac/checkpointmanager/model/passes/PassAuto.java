package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.car.Car;

import java.util.Objects;


@Entity
@Setter
@Getter
@DiscriminatorValue("AUTO")
public class PassAuto extends Pass {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "car_id")
    private Car car;

    @Override
    public boolean compareByFields(Pass o) {
        PassAuto other = (PassAuto) o;
        return (super.compareByFields(other) &&
                Objects.equals(this.getCar().getLicensePlate(), other.getCar().getLicensePlate()));
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                super.toString() +
                ", c=" + car.getLicensePlate() +
                '}';
    }
}
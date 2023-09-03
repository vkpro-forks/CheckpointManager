package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.model.car.Car;

import java.util.List;
import java.util.UUID;

public interface CarService {

    Car addCar(Car car);

    void deleteCar(UUID carId);

    Car updateCar(UUID carId, Car car);

    Car getCarById(UUID carId);

    List<Car> getAllCars();
}

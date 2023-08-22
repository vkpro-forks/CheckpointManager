package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Car;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarService {

    Car addCar(Car car);

    void deleteCar(UUID carId);

    Car updateCar(UUID carId, Car car);

    Car getCarById(UUID carId);

    List<Car> getAllCars();
}

package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.model.car.Car;

import java.util.List;
import java.util.UUID;

public interface CarService {

    Car addCar(Car car);

    void deleteCar(UUID carId);

    Car updateCar(String carId, CarDTO updateCar);

    Car getCarById(UUID carId);

    List<Car> getAllCars();

    List<Car> findByPhonePart(String phone);

    List<Car> findByUserId(UUID userId);

    boolean existsById(UUID id);
}

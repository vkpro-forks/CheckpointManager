package ru.ac.checkpointmanager.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.Car;
import ru.ac.checkpointmanager.repository.CarRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    public CarServiceImpl(CarRepository carRepository) {
        this.carRepository = carRepository;
    }


    @Override
    public Car addCar(Car car) {
        return carRepository.save(car);
    }

    @Override
    public void deleteCar(UUID carId) {
        try {
            carRepository.deleteById(carId);
        } catch (EmptyResultDataAccessException ex) {
            throw new EntityNotFoundException("Car with ID " + carId + " not found");
        } catch (Exception exception) {
            throw new RuntimeException("Error deleting car with ID " + carId);
        }
    }

    @Override
    public Car updateCar(UUID carId, Car updateCar) {
        try {
            Car existingCar = carRepository.findById(carId)
                    .orElseThrow(() -> new EntityNotFoundException("Car with ID " + carId + " not found"));

            existingCar.setLicensePlate(updateCar.getLicensePlate());
            existingCar.setBrandModel(updateCar.getBrandModel());
            existingCar.setType(updateCar.getType());
            existingCar.setColor(updateCar.getColor());
            existingCar.setYear(updateCar.getYear());
            existingCar.setPhoto(updateCar.getPhoto());

            return carRepository.save(existingCar);
        } catch (Exception exception) {
            throw new RuntimeException("Error updating car with ID " + carId);
        }
    }

    @Override
    public Car getCarById(UUID carId) {
        return carRepository.findById(carId)
                .orElseThrow(()-> new EntityNotFoundException("Car with ID " + carId + " not found"));
    }

    @Override
    public List<Car> getAllCars() {
        List<Car> cars = carRepository.findAll();

        if (cars.isEmpty()) {
            System.out.println("No cars found in the database.");
        }

        return cars;
    }
}

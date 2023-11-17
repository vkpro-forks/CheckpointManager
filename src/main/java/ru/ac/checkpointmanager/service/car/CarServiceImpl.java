package ru.ac.checkpointmanager.service.car;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarNotFoundException;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository repository;
    private final UserService userService;

    @Override
    public Car addCar(Car car) {
        return repository.save(car);
    }

    @Override
    public Car getCarById(UUID carId) {
        return repository.findById(carId)
                .orElseThrow(()-> new CarNotFoundException("Car with ID " + carId + " not found"));
    }

    @Override
    public void deleteCar(UUID carId) {
        try {
            repository.deleteById(carId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CarNotFoundException("Car with ID " + carId + " not found");
        } catch (Exception exception) {
            throw new RuntimeException("Error deleting car with ID " + carId);
        }
    }

    @Override
    public Car updateCar(UUID carId, Car updateCar) {
        try {
            Car existingCar = repository.findById(carId)
                    .orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found"));

            existingCar.setLicensePlate(updateCar.getLicensePlate());
            existingCar.setBrand(updateCar.getBrand());

            return repository.save(existingCar);
        } catch (Exception exception) {
            throw new RuntimeException("Error updating car with ID " + carId);
        }
    }

    @Override
    public List<Car> getAllCars() {
        List<Car> cars = repository.findAll();

        if (cars.isEmpty()) {
            System.out.println("No cars found in the database.");
        }

        return cars;
    }

    @Override
    public List<Car> findByUserId(UUID userId) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), userId);
        userService.findById(userId);

        List<Car> foundCars = repository.findCarsByUserId(userId);
        log.debug("Find {} cars for user [UUID - {}]", foundCars.size(), userId);
        return foundCars;
    }
}

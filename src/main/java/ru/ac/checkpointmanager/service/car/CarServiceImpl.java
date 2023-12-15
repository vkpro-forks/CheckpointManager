package ru.ac.checkpointmanager.service.car;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.CarDTO;
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

    public static final String CAR_NOT_FOUND_LOG = "[Car with id: {}] not found";
    public static final String CAR_NOT_FOUND_MSG = "Car with id %s not found";
    private final CarRepository repository;
    private final UserService userService;

    @Override
    public Car addCar(Car car) {
        car.setId(UUID.randomUUID());
        log.info("Adding new Car: {}", car);
        return repository.save(car);
    }


    @Override
    public Car getCarById(UUID carId) {
        Car car = repository.findById(carId)
                .orElseThrow(() -> {
                    log.warn(CAR_NOT_FOUND_LOG, carId);
                    return new CarNotFoundException(CAR_NOT_FOUND_MSG.formatted(carId));
                });
        log.debug("[Car with id: {}] successfully retrieved from repo", car);
        return car;
    }

    @Override
    public void deleteCar(UUID carId) {
        if (!repository.existsById(carId)) {
            log.warn(CAR_NOT_FOUND_LOG, carId);
            throw new CarNotFoundException(CAR_NOT_FOUND_MSG.formatted(carId));
        }
        repository.deleteById(carId);
        log.info("[Car with id: {}] successfully deleted", carId);
    }

    @Override
    public Car updateCar(String carId, CarDTO updateCar) {
        Car existingCar = repository.findById(UUID.fromString(carId))
                .orElseThrow(() -> {
                    log.warn(CAR_NOT_FOUND_LOG, carId);
                    return new CarNotFoundException(CAR_NOT_FOUND_MSG.formatted(carId));
                });
        existingCar.setLicensePlate(updateCar.getLicensePlate());
        existingCar.setBrand(updateCar.getBrand());
        Car saved = repository.save(existingCar);
        log.info("[Car with id: {}] successfully updated", carId);
        return saved;
    }

    @Override
    public List<Car> getAllCars() {
        return repository.findAll();
    }

    @Override
    public List<Car> findByPhonePart(String phone) {
        log.info("Searching for Cars with phone containing: {}", phone);
        return repository.findByPhoneContaining(phone);
    }

    @Override
    public List<Car> findByUserId(UUID userId) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), userId);
        userService.findById(userId);

        List<Car> foundCars = repository.findCarsByUserId(userId);
        log.debug("Find {} cars for user [UUID - {}]", foundCars.size(), userId);
        return foundCars;
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}

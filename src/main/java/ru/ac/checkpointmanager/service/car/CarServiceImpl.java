package ru.ac.checkpointmanager.service.car;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.CarNotFoundException;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class CarServiceImpl implements CarService {

    public static final String CAR_NOT_FOUND_LOG = "[Car with id: {}] not found";
    public static final String CAR_NOT_FOUND_MSG = "Car with id %s not found";
    public static final String CAR_BRAND_NOT_FOUND = "CarBrand with [name: %s] not found";
    private final CarRepository carRepository;

    private final CarBrandRepository carBrandRepository;

    private final UserService userService;

    @Override
    @Transactional
    public Car addCar(Car car) {
        car.setId(UUID.randomUUID());
        String carBrandName = car.getBrand().getBrand();
        CarBrand carBrand = carBrandRepository.findByBrand(carBrandName)
                .orElseThrow(() -> {
                    log.warn(CAR_BRAND_NOT_FOUND.formatted(carBrandName));
                    return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND.formatted(carBrandName));
                });
        car.setBrand(carBrand);
        Car savedCar = carRepository.save(car);
        log.info("Car [id: {}] successfully saved", savedCar.getId());
        return savedCar;
    }

    @Override
    public Car getCarById(UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> {
                    log.warn(CAR_NOT_FOUND_LOG, carId);
                    return new CarNotFoundException(CAR_NOT_FOUND_MSG.formatted(carId));
                });
        log.debug("[Car with id: {}] successfully retrieved from repo", car);
        return car;
    }

    @Override
    public void deleteCar(UUID carId) {
        if (!carRepository.existsById(carId)) {
            log.warn(CAR_NOT_FOUND_LOG, carId);
            throw new CarNotFoundException(CAR_NOT_FOUND_MSG.formatted(carId));
        }
        carRepository.deleteById(carId);
        log.info("[Car with id: {}] successfully deleted", carId);
    }

    @Override
    public Car updateCar(UUID carId, CarDTO updateCar) {
        Car existingCar = carRepository.findById(carId)
                .orElseThrow(() -> {
                    log.warn(CAR_NOT_FOUND_LOG, carId);
                    return new CarNotFoundException(CAR_NOT_FOUND_MSG.formatted(carId));
                });
        existingCar.setLicensePlate(updateCar.getLicensePlate());
        String carBrandName = updateCar.getBrand().getBrand();
        Optional<CarBrand> carBrandOptional = carBrandRepository.findByBrand(carBrandName);
        CarBrand carBrandToUpdate = carBrandOptional.orElseThrow(() -> {
            log.warn(CAR_BRAND_NOT_FOUND.formatted(carBrandName));
            return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND.formatted(carBrandName));
        });
        existingCar.setBrand(carBrandToUpdate);
        Car saved = carRepository.save(existingCar);
        log.info("[Car with id: {}] successfully updated", carId);
        return saved;
    }

    @Override
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @Override
    public List<Car> findByPhonePart(String phone) {
        log.info("Searching for Cars with phone containing: {}", phone);
        return carRepository.findByPhoneContaining(phone);
    }

    @Override
    public List<Car> findByUserId(UUID userId) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), userId);
        userService.findById(userId);

        List<Car> foundCars = carRepository.findCarsByUserId(userId);
        log.debug("Find {} cars for user [UUID - {}]", foundCars.size(), userId);
        return foundCars;
    }

    @Override
    public boolean existsById(UUID id) {
        return carRepository.existsById(id);
    }
}

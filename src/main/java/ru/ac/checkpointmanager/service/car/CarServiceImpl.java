package ru.ac.checkpointmanager.service.car;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarNotFoundException;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.repository.car.CarRepository;

import java.util.List;
import java.util.UUID;

@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarBrandService carBrandService;

    private final CarModelService carModelService;

    public CarServiceImpl(CarRepository carRepository, CarBrandService carBrandService, CarModelService carModelService) {
        this.carRepository = carRepository;
        this.carBrandService = carBrandService;
        this.carModelService = carModelService;
    }


    @Override
    public Car addCar(Car car) {
        return carRepository.save(car);
    }

    @Override
    public Car getCarById(UUID carId) {
        return carRepository.findById(carId)
                .orElseThrow(()-> new CarNotFoundException("Car with ID " + carId + " not found"));
    }

    @Override
    public void deleteCar(UUID carId) {
        try {
            carRepository.deleteById(carId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CarNotFoundException("Car with ID " + carId + " not found");
        } catch (Exception exception) {
            throw new RuntimeException("Error deleting car with ID " + carId);
        }
    }

    @Override
    public Car updateCar(UUID carId, Car updateCar) {
        try {
            Car existingCar = carRepository.findById(carId)
                    .orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found"));

            existingCar.setLicensePlate(updateCar.getLicensePlate());
            existingCar.setBrand(updateCar.getBrand());
            existingCar.setType(updateCar.getType());
            existingCar.setColor(updateCar.getColor());
            existingCar.setYear(updateCar.getYear());

            return carRepository.save(existingCar);
        } catch (Exception exception) {
            throw new RuntimeException("Error updating car with ID " + carId);
        }
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

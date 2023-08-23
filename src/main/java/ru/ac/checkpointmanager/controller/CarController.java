package ru.ac.checkpointmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.Car;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.service.CarBrandService;
import ru.ac.checkpointmanager.service.CarService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/car")
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;

    public CarController(CarService carService, CarBrandService carBrandService) {
        this.carService = carService;
        this.carBrandService = carBrandService;
    }

    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        List<Car> carList = carService.getAllCars();
        if (carList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(carList);
    }

    @PostMapping
    public ResponseEntity<String> addCar(@RequestBody Car carRequest) {
        if (!validateLicensePlate(carRequest.getLicensePlate())) {
            return ResponseEntity.badRequest().body("Invalid license plate");
        }

        if (carRequest.getLicensePlate().length() > 9) {
            return ResponseEntity.badRequest().body("License plate should not exceed 9 characters");
        }

        CarBrand brand = carBrandService.getBrandById(carRequest.getBrand().getId());

        if (brand == null) {
            return ResponseEntity.badRequest().body("Invalid brand ID");
        }

        Car car = new Car();
        car.setLicensePlate(carRequest.getLicensePlate());
        car.setBrand(brand);
        car.setModel(carRequest.getModel());
        car.setType(carRequest.getType());
        car.setColor(carRequest.getColor());
        car.setYear(carRequest.getYear());

        Car addedCar = carService.addCar(carRequest);
        return ResponseEntity.ok("Car added with ID: " + addedCar.getId());
    }

    @PutMapping("/{carId}")
    public ResponseEntity<Car> updateCar(@PathVariable UUID carId, @RequestBody Car updateCar) {
        Car updated = carService.updateCar(carId, updateCar);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    private boolean validateLicensePlate(String licensePlate) {
        String invalidLetters = "йцгшщзфыплджэячьъбю";
        for (char letter : invalidLetters.toCharArray()) {
            if (licensePlate.contains(String.valueOf(letter))) {
                return false;
            }
        }
        return true;
    }

}

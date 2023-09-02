package ru.ac.checkpointmanager.controller.car;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.car.CarModel;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.service.car.CarModelService;
import ru.ac.checkpointmanager.service.car.CarService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;
    private final CarModelService carModelService;

    private boolean validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.length() < 6 || licensePlate.length() > 10) {
            return true;
        }
        String invalidLetters = "йцгшщзфыплджэячьъбю";
        for (char letter : invalidLetters.toCharArray()) {
            if (licensePlate.contains(String.valueOf(letter))) {
                return true;
            }
        }
        return !licensePlate.matches("[a-zA-Z0-9]+");
    }

    @PostMapping
    public ResponseEntity<?> addCar(@RequestBody Car carRequest) {
        if (validateLicensePlate(carRequest.getLicensePlate())) {
            return new ResponseEntity<>("Invalid license plate", HttpStatus.BAD_REQUEST);
        }

        CarBrand existingBrand = carBrandService.getBrandById(carRequest.getBrand().getId());
        CarModel existingModel = carModelService.getModelById(carRequest.getModel().getId());
        carRequest.setBrand(existingBrand);
        carRequest.setModel(existingModel);
        Car newCar = carService.addCar(carRequest);
        return new ResponseEntity<>(newCar, HttpStatus.CREATED);
    }

    @PutMapping("/{carId}")
    public ResponseEntity<?> updateCar(@PathVariable UUID carId, @RequestBody Car updateCar) {
        if (validateLicensePlate(updateCar.getLicensePlate())) {
            return ResponseEntity.badRequest().body("Invalid license plate");
        }
        Car updated = carService.updateCar(carId, updateCar);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        List<Car> carList = carService.getAllCars();
        if (carList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(carList);
    }
}

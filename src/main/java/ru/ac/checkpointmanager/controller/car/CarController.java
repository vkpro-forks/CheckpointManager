package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("chpman/car")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;
    private final CarModelService carModelService;

    @PostMapping
    public ResponseEntity<?> addCar(@RequestBody Car carRequest) {
        try {
            CarBrand existingBrand = carBrandService.getBrandById(carRequest.getBrand().getId());
            CarModel existingModel = carModelService.getModelById(carRequest.getModel().getId());
            carRequest.setBrand(existingBrand);
            carRequest.setModel(existingModel);
            Car newCar = carService.addCar(carRequest);
            return new ResponseEntity<>(newCar, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{carId}")
    public ResponseEntity<?> updateCar(@PathVariable UUID carId, @RequestBody Car updateCar) {
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

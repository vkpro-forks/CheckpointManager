package ru.ac.checkpointmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.Car;
import ru.ac.checkpointmanager.service.CarService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/car")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
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
    public ResponseEntity<Car> addCar(@RequestBody Car car) {
        Car addedCar = carService.addCar(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCar);
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

}

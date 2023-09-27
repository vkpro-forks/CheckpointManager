package ru.ac.checkpointmanager.controller.car;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.car.CarModel;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.service.car.CarModelService;
import ru.ac.checkpointmanager.service.car.CarService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;
    private final CarModelService carModelService;

    @PostMapping
    public ResponseEntity<?> addCar(@Valid @RequestBody Car carRequest, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

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
    public ResponseEntity<?> updateCar(@Valid @PathVariable UUID carId, @RequestBody Car updateCar, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
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

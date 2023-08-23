package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.Car;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.service.CarBrandService;
import ru.ac.checkpointmanager.service.CarModelService;
import ru.ac.checkpointmanager.service.CarService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/car")
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;
    private final CarModelService carModelService;

    public CarController(CarService carService, CarBrandService carBrandService, CarModelService carModelService) {
        this.carService = carService;
        this.carBrandService = carBrandService;
        this.carModelService = carModelService;
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

        CarModel model = carModelService.getModelById(carRequest.getModel().getId());

        if (model == null) {
            return ResponseEntity.badRequest().body("Invalid brand ID");
        }

        Car car = new Car();
        car.setLicensePlate(carRequest.getLicensePlate());
        car.setBrand(brand);
        car.setModel(model);
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

    //===================================controllers for brands==========================================//

    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@Valid @RequestBody CarBrand brand, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("Validation error", HttpStatus.BAD_REQUEST);
        }
        CarBrand carBrand = carBrandService.addBrand(brand);
        return new ResponseEntity<>(carBrand, HttpStatus.CREATED);
    }

    @GetMapping("/brands/{id}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long id) {
        CarBrand brand = carBrandService.getBrandById(id);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }

    @DeleteMapping("/brands/{id}")
    public ResponseEntity<Void> deleteCarBrandById(@PathVariable Long id) {
        carBrandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<CarBrand> updateCarBrand(@PathVariable Long id,
                                                   @Valid @RequestBody CarBrand carBrandDetails,
                                                   BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        CarBrand updateCarBrand;
        try {
            updateCarBrand = carBrandService.updateBrand(id, carBrandDetails);
        } catch (CarBrandNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updateCarBrand, HttpStatus.OK);
    }

    //===================================controllers for models==============================================//


    @PostMapping("/models")
    public ResponseEntity<?> createModel(@Valid @RequestBody CarModel model, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("Validation error", HttpStatus.BAD_REQUEST);
        }
        CarModel carModel = carModelService.addModel(model);
        return new ResponseEntity<>(carModel, HttpStatus.CREATED);
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<CarModel> getCarModelById(@PathVariable Long id) {
        CarModel model = carModelService.getModelById(id);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @DeleteMapping("/models/{id}")
    public ResponseEntity<Void> deleteCarModelById(@PathVariable Long id) {
        carModelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/models/{id}")
    public ResponseEntity<CarModel> updateCarModel(@PathVariable Long id,
                                                   @Valid @RequestBody CarModel carModelDetails,
                                                   BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        CarModel updateCarModel;
        try {
            updateCarModel = carModelService.updateModel(id, carModelDetails);
        } catch (CarModelNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updateCarModel, HttpStatus.OK);
    }
}

package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.service.CarBrandService;
import ru.ac.checkpointmanager.service.CarModelService;
import ru.ac.checkpointmanager.service.CarService;

import java.util.List;
@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarBrandAndModelController {


    private final CarService carService;
    private final CarBrandService carBrandService;
    private final CarModelService carModelService;

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
                                                   @RequestBody CarBrand carBrandDetails) {
        CarBrand updateCarBrand;
        try {
            updateCarBrand = carBrandService.updateBrand(id, carBrandDetails);
        } catch (CarBrandNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updateCarBrand, HttpStatus.OK);
    }

    @GetMapping("/brands/all")
    public ResponseEntity<List<CarBrand>> getAllBrands() {
        List<CarBrand> allBrands = carBrandService.getAllBrands();

        if (allBrands.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(allBrands, HttpStatus.OK);
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
                                                   @RequestBody CarModel carModelDetails) {
        CarModel updateCarModel;
        try {
            updateCarModel = carModelService.updateModel(id, carModelDetails);
        } catch (CarModelNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(updateCarModel, HttpStatus.OK);
    }
    @GetMapping("/models/all")
    public ResponseEntity<List<CarModel>> getAllModels() {
        List<CarModel> allModels = carModelService.getAllModels();

        if (allModels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(allModels, HttpStatus.OK);
    }


}

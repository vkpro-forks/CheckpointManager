package ru.ac.checkpointmanager.controller.car;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.car.CarModel;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.service.car.CarModelService;
import ru.ac.checkpointmanager.utils.ApiError;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarModelController {

    private final CarModelService carModelService;
    private final CarBrandService carBrandService;
    private static final String NOT_FOUND_MESSAGE = " not found with ID: ";


    @PostMapping("/models")
    public ResponseEntity<?> createModel(@Valid @RequestBody CarModel model, BindingResult result) {

        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        CarModel carModel = carModelService.addModel(model);
        return new ResponseEntity<>(carModel, HttpStatus.CREATED);
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<?> getCarModelById(@PathVariable Long id) {
        CarModel model = carModelService.getModelById(id);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @DeleteMapping("/models/{id}")
    public ResponseEntity<String> deleteCarModelById(@PathVariable Long id) {
        carModelService.deleteModel(id);
        return new ResponseEntity<>("Model with ID " + id + " deleted", HttpStatus.OK);
    }

    @PutMapping("/models/{id}")
    public ResponseEntity<?> updateCarModel(@PathVariable Long id, @Valid
    @RequestBody CarModel carModelDetails, BindingResult result) {

        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        CarModel carModel = carModelService.updateModel(id, carModelDetails);
        return new ResponseEntity<>(carModel, HttpStatus.OK);
    }

    @GetMapping("/models/all")
    public ResponseEntity<List<CarModel>> getAllModels() {
        List<CarModel> allModels = carModelService.getAllModels();
        if (allModels.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allModels, HttpStatus.OK);
    }

    @GetMapping("/model-name")
    public ResponseEntity<CarModel> getModelByName(@RequestParam String modelNamePart) {
        CarModel model = carModelService.findByModelContainingIgnoreCase(modelNamePart);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @GetMapping("/model-brandId")
    public ResponseEntity<?> getAllModelsByBrandId(@RequestParam Long brandId) {
        List<CarModel> models = carBrandService.findModelsByBrandId(brandId);
        if (models.isEmpty()) {
            return new ResponseEntity<>("No models " + NOT_FOUND_MESSAGE + brandId, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(models, HttpStatus.OK);
    }
}

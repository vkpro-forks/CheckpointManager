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
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarModelController {

    private final CarModelService carModelService;
    private final CarBrandService carBrandService;
    private static final String NOT_FOUND_MESSAGE = " not found with ID: ";

    private boolean isInvalidModelName(String modelName) {
        return !modelName.matches("^[a-zA-Zа-яА-Я0-9\\s-]+$");
    }

    private Map<String, String> getErrors(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return errors;
    }

    @PostMapping("/models")
    public ResponseEntity<?> createModel(@Valid @RequestBody CarModel model, BindingResult result) {

        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        try {
            CarModel carModel = carModelService.addModel(model);
            return new ResponseEntity<>(carModel, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>("Car brand not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred, " +
                    "you need to enter an Brand ID and Brand name",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<?> getCarModelById(@PathVariable Long id) {
        try {
            CarModel model = carModelService.getModelById(id);
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (CarModelNotFoundException e) {
            return new ResponseEntity<>("Id model not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/models/{id}")
    public ResponseEntity<String> deleteCarModelById(@PathVariable Long id) {
        try {
            carModelService.deleteModel(id);
            return new ResponseEntity<>("Model with ID " + id + " deleted", HttpStatus.OK);
        } catch (CarModelNotFoundException e) {
            return new ResponseEntity<>("Id model not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/models/{id}")
    public ResponseEntity<?> updateCarModel(@PathVariable Long id, @RequestBody CarModel carModelDetails) {
        if (isInvalidModelName(carModelDetails.getModel())) {
            return new ResponseEntity<>("Model name should contain only letters, " +
                    "spaces, and underscores", HttpStatus.BAD_REQUEST);
        }
        try {
            CarModel carModel = carModelService.updateModel(id, carModelDetails);
            return new ResponseEntity<>(carModel, HttpStatus.OK);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(getErrors(e), HttpStatus.BAD_REQUEST);
        } catch (CarModelNotFoundException e) {
            return new ResponseEntity<>("Model not found", HttpStatus.NOT_FOUND);
        }
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
        try {
            List<CarModel> models = carBrandService.findModelsByBrandId(brandId);
            if (models.isEmpty()) {
                return new ResponseEntity<>("No models " + NOT_FOUND_MESSAGE + brandId, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(models, HttpStatus.OK);
        } catch (CarBrandNotFoundException e) {
            return new ResponseEntity<>("Car brand " + NOT_FOUND_MESSAGE + brandId, HttpStatus.NOT_FOUND);
        }
    }
}

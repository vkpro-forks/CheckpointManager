package ru.ac.checkpointmanager.controller.car;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.car.CarModel;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.service.car.CarModelService;
import ru.ac.checkpointmanager.service.car.CarService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarBrandController {

    private final CarBrandService carBrandService;

    private ResponseEntity<Map<String, String>> handleValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : result.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@Valid @RequestBody CarBrand brand, BindingResult result) {
        if (result.hasErrors()) {
            return handleValidationErrors(result);
        }
        if (!brand.getBrand().matches("^[a-zA-Z-\\s]+$")) {
            return new ResponseEntity<>("Brand name should contain only letters," +
                    " spaces, and underscores", HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<String> deleteCarBrandById(@PathVariable Long id) {
        try {
            carBrandService.deleteBrand(id);
            return ResponseEntity.noContent().build();
        } catch (CarBrandNotFoundException e) {
            return new ResponseEntity<>("Id brand not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<?> updateCarBrand(@Valid @PathVariable Long id,
                                            @RequestBody CarBrand carBrandDetails,
                                            BindingResult result) {
        if (result.hasErrors()) {
            return handleValidationErrors(result);
        }
        if (!carBrandDetails.getBrand().matches("^[a-zA-Zа-яА-Я0-9\\s-]+$")) {
            return new ResponseEntity<>("Brand name should contain only letters, " +
                    "spaces, and underscores", HttpStatus.BAD_REQUEST);
        }
        try {
            CarBrand carBrand = carBrandService.updateBrand(id, carBrandDetails);
            return new ResponseEntity<>(carBrand, HttpStatus.OK);
        } catch (ConstraintViolationException e) {
            return handleValidationErrors(result);
        } catch (CarBrandNotFoundException e) {
            return new ResponseEntity<>("Brand not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/brands/all")
    public ResponseEntity<List<CarBrand>> getAllBrands() {
        List<CarBrand> allBrands = carBrandService.getAllBrands();
        if (allBrands.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allBrands, HttpStatus.OK);
    }

    @GetMapping("/brands-name")
    public ResponseEntity<List<CarBrand>> getBrandsByName(@RequestParam String brandNamePart) {
        List<CarBrand> brands = carBrandService.findByBrandsContainingIgnoreCase(brandNamePart);
        if (brands == null || brands.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(brands, HttpStatus.OK);
    }
}

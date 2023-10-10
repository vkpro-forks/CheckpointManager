package ru.ac.checkpointmanager.controller.car;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;

@RestController
@RequestMapping("chpman/car")
@RequiredArgsConstructor
public class CarBrandController {

    private final CarBrandService carBrandService;

    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@Valid @RequestBody CarBrand brand, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        CarBrand carBrand = carBrandService.addBrand(brand);
        return new ResponseEntity<>(carBrand, HttpStatus.CREATED);
    }

    @GetMapping("/brands/{id}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long id) {
        CarBrand brand = carBrandService.getBrandById(id);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }

    //удалить бренд можно только в том случае, если у этого бренда в бд нет ни одной модели
    @DeleteMapping("/brands/{id}")
    public ResponseEntity<String> deleteCarBrandById(@PathVariable Long id) {
            carBrandService.deleteBrand(id);
            return ResponseEntity.noContent().build();
    }

    //удаляем бренд и все модели которые к нему привязаны
    @DeleteMapping("/brands-all-models/{id}")
    public ResponseEntity<String> deleteCarBrandByIdWithAllModelsByBrand(@PathVariable Long id) {
            carBrandService.deleteBrandAndAllModelsByBrand(id);
            return ResponseEntity.noContent().build();
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<?> updateCarBrand(@Valid @PathVariable Long id,
                                            @RequestBody CarBrand carBrandDetails,
                                            BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

            CarBrand carBrand = carBrandService.updateBrand(id, carBrandDetails);
            return new ResponseEntity<>(carBrand, HttpStatus.OK);
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

package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;

@RestController
@RequestMapping("chpman/car")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CarBrand (Бренд Авто)", description = "Для обработки Брендов Авто")
@ApiResponses(value = {@ApiResponse(responseCode = "401",
        description = "Произошла ошибка, Нужно авторизоваться")})
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class CarBrandController {

    private final CarBrandService carBrandService;

    @Operation(summary = "Создание нового бренда")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Создание произошло успешно"),
            @ApiResponse(responseCode = "400", description = "Не уадалось создать бренд"),
    })
    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@Valid @RequestBody CarBrand brand, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        CarBrand carBrand = carBrandService.addBrand(brand);
        return new ResponseEntity<>(carBrand, HttpStatus.CREATED);
    }

    @Operation(summary = "Получение бренда по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд получен"),
            @ApiResponse(responseCode = "404", description = "Такого бренда не существует."),
    })
    @GetMapping("/brands/{id}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long id) {
        CarBrand brand = carBrandService.getBrandById(id);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }


    @Operation(summary = "Удалить бренд по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Удалось удалить бренд"),
            @ApiResponse(responseCode = "400", description = "Неправильный запрос"),
            @ApiResponse(responseCode = "404", description = "Нет такого бренда по этому Id"),
    })
    @DeleteMapping("/brands/{id}")
    public ResponseEntity<String> deleteCarBrandById(@PathVariable Long id) {
            carBrandService.deleteBrand(id);
            return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновление бренда по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд обновлен благополучно"),
            @ApiResponse(responseCode = "400", description = "Не удалось обновить бренд"),
    })
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

    @Operation(summary = "Вывести список всех брендов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список брендов успешно создан"),
            @ApiResponse(responseCode = "404", description = "Нет ни одного бренда в бд"),
    })
    @GetMapping("/brands/all")
    public ResponseEntity<List<CarBrand>> getAllBrands() {
        List<CarBrand> allBrands = carBrandService.getAllBrands();
        if (allBrands.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allBrands, HttpStatus.OK);
    }

    @Operation(summary = "Поиск бренда по имени или части имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд найден>"),
            @ApiResponse(responseCode = "404", description = "Бренд не найден>"),
    })
    @GetMapping("/brands-name")
    public ResponseEntity<List<CarBrand>> getBrandsByName(@RequestParam String brandNamePart) {
        List<CarBrand> brands = carBrandService.findByBrandsContainingIgnoreCase(brandNamePart);
        if (brands == null || brands.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(brands, HttpStatus.OK);
    }
}

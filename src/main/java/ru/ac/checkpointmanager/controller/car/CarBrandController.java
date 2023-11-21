package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("chpman/car")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CarBrand (Бренд Авто)", description = "Для обработки Брендов Авто")
@ApiResponses(value = {@ApiResponse(responseCode = "401",
        description = "Произошла ошибка, Нужно авторизоваться")})
public class CarBrandController {

    private final CarBrandService carBrandService;

    @Operation(summary = "Добавить новый БрендАвто",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "БрендАвто успешно добавлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@Valid @RequestBody CarBrand brand, BindingResult result) {
        if (result.hasErrors()) {
            log.warn("Creation failed: Invalid CarBrand data");
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        CarBrand carBrand = carBrandService.addBrand(brand);
        log.info("CarBrand created: {}", carBrand);
        return new ResponseEntity<>(carBrand, HttpStatus.CREATED);
    }

    @Operation(summary = "Получение бренда по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "404", description = "Такого бренда не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands/{id}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long id) {
        CarBrand brand = carBrandService.getBrandById(id);
        if (brand == null) {
            log.warn("CarBrand with ID {} not found", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.debug("Retrieved CarBrand by ID: {}", brand);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }


    @Operation(summary = "Удалить БрендАвто",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "БрендАвто успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "404", description = "Такого бренда не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/brands/{id}")
    public ResponseEntity<String> deleteCarBrandById(@PathVariable Long id) {
        carBrandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить новый БрендАвто",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "БрендАвто успешно обновлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/brands/{id}")
    public CarBrand updateCarBrand(@Valid @PathVariable Long id,
                                   @Valid @RequestBody CarBrand carBrandDetails) {
        return carBrandService.updateBrand(id, carBrandDetails);
    }

    @Operation(summary = "Получение всех брендов.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список брендов получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarBrand.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands/all")
    public ResponseEntity<List<CarBrand>> getAllBrands() {
        List<CarBrand> allBrands = carBrandService.getAllBrands();
        if (allBrands.isEmpty()) {
            log.warn("No CarBrands found");
        }
        log.debug("Retrieved all CarBrands");
        return new ResponseEntity<>(allBrands, HttpStatus.OK);
    }

    @Operation(summary = "Получение брендов по части имени.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список брендов получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarBrand.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands-name")
    public ResponseEntity<List<CarBrand>> getBrandsByName(@RequestParam String brandNamePart) {
        List<CarBrand> brands = carBrandService.findByBrandsContainingIgnoreCase(brandNamePart);
        if (brands == null || brands.isEmpty()) {
            log.warn("No CarBrands found containing '{}'", brandNamePart);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.debug("Retrieved CarBrands by name part: {}", brands);
        return new ResponseEntity<>(brands, HttpStatus.OK);
    }
}

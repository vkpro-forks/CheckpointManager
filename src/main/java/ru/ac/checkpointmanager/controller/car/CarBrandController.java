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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/car")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CarBrand (Бренд Машины)", description = "Для обработки Брендов Авто")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "Произошла ошибка, Нужно авторизоваться"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
public class CarBrandController {

    private final CarBrandService carBrandService;

    @Operation(summary = "Добавить новый Бренд Машины",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Бренд Машины успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public CarBrand createBrand(@Valid @RequestBody CarBrandDTO brand) {
        return carBrandService.addBrand(brand);
    }

    @Operation(summary = "Получение Бренд Машины по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд Машины получен.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "404", description = "Такого Бренд Машины не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands/{id}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long id) {
        CarBrand brand = carBrandService.getBrandById(id);
        log.debug("Retrieved CarBrand by ID: {}", brand);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }


    @Operation(summary = "Удалить Бренд Машины",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Бренд Машины успешно удален",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "404", description = "Такого Бренд Машины не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/brands/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCarBrandById(@PathVariable Long id) {
        carBrandService.deleteBrand(id);
    }

    @Operation(summary = "Обновить новый Бренд Машины",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бренд Машины успешно обновлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/brands/{id}")
    public CarBrand updateCarBrand(@Valid @PathVariable Long id,
                                   @Valid @RequestBody CarBrandDTO carBrandDetails) {
        return carBrandService.updateBrand(id, carBrandDetails);
    }

    @Operation(summary = "Получение всех Бренд Машины.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список Бренд Машины получен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands/all")
    public ResponseEntity<List<CarBrand>> getAllBrands() {
        List<CarBrand> allBrands = carBrandService.getAllBrands();
        log.debug("Retrieved all CarBrands");
        return new ResponseEntity<>(allBrands, HttpStatus.OK);
    }

    @Operation(summary = "Получение Бренд Машины по части имени.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список Бренд Машины получен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands-name")
    public ResponseEntity<List<CarBrand>> getBrandsByName(@RequestParam String brandNamePart) {
        List<CarBrand> brands = carBrandService.findByBrandsContainingIgnoreCase(brandNamePart);
        log.debug("Retrieved CarBrands by name part: {}", brands);
        return new ResponseEntity<>(brands, HttpStatus.OK);
    }
}

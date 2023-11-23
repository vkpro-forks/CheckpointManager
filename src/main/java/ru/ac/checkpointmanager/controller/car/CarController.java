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
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.mapper.CarMapper;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.service.car.CarService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("chpman/car")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Car (Машина)", description = "Для обработки списка машин")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})

public class CarController {

    private final CarMapper mapper;
    private final CarService carService;
    private final CarBrandService carBrandService;

    @Operation(summary = "Добавить новую машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Машина успешно добавлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PostMapping
    public ResponseEntity<?> addCar(@Valid @RequestBody CarDTO carDTO, BindingResult result) {
        if (result.hasErrors()) {
            log.warn("Car creation failed due to validation errors");
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        Car car = mapper.toCar(carDTO);

        try {
            CarBrand existingBrand = carBrandService.getBrandById(carDTO.getBrand().getId());
            car.setBrand(existingBrand);
            Car newCar = carService.addCar(car);
            CarDTO newCarDTO = mapper.toCarDTO(newCar);
            log.info("New car created: {}", newCar);
            return new ResponseEntity<>(newCarDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Car creation failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @Operation(summary = "Обновить новую машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Машина успешно обновлена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/{carId}")
    public ResponseEntity<?> updateCar(@org.hibernate.validator.constraints.UUID
                                       @PathVariable String carId,
                                       @Valid @RequestBody CarDTO updateCarDto,
                                       BindingResult result) {
        if (result.hasErrors()) {
            log.warn("Car update failed for ID {} due to validation errors", carId);
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        Car updated = carService.updateCar(carId, updateCarDto);
        log.info("[Car with id {}] updated", carId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Машина успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
            @ApiResponse(responseCode = "404", description = "Такой машины не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получение всех машин.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список машин получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<Car> carList = carService.getAllCars();
        log.debug("Retrieved all cars");
        return ResponseEntity.ok(mapper.toCarDTOs(carList));
    }

    @Operation(summary = "Получение всех машин по user.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список машин получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CarDTO>> searchByUserId(@PathVariable UUID userId) {
        List<Car> cars = carService.findByUserId(userId);
        List<CarDTO> carDTOs = mapper.toCarDTOs(cars);
        log.debug("Retrieved cars for user ID {}", userId);
        return new ResponseEntity<>(carDTOs, HttpStatus.OK);
    }
}

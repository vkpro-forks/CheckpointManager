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
@Tag(name = "Car (Машина)", description = "Для обработки списка машин")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "Произошла ошибка, Нужно авторизоваться"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")
})
@Tag(name = "Car (Авто)", description = "Для обработки списка Авто")
@ApiResponses(value = {@ApiResponse(responseCode = "401",
        description = "Произошла ошибка, Нужно авторизоваться")})
@SecurityRequirement(name = "bearerAuth")
public class CarController {

    private final CarMapper mapper;
    private final CarService carService;
    private final CarBrandService carBrandService;

    @Operation(summary = "Добавить новый Авто",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Авто успешно добавлен",
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

    @Operation(summary = "Обновить новый Авто",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Авто успешно обновлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/{carId}")
    public ResponseEntity<?> updateCar(@Valid @PathVariable UUID carId, @RequestBody CarDTO updateCarDto, BindingResult result) {
        if (result.hasErrors()) {
            log.warn("Car update failed for ID {} due to validation errors", carId);
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        Car car = mapper.toCar(updateCarDto);

        Car updated =  carService.updateCar(carId, car);
        log.info("Car with ID {} updated", carId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить Авто",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Авто успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
            @ApiResponse(responseCode = "404", description = "Такого Авто не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получение всех Авто.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список Авто получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<Car> carList = carService.getAllCars();
        if (carList.isEmpty()) {
            log.debug("No cars found");
            return ResponseEntity.noContent().build();
        }
        log.debug("Retrieved all cars");
        return ResponseEntity.ok(mapper.toCarDTOs(carList));
    }

    @Operation(summary = "Получение всех Авто по user.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список Авто получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Car.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CarDTO>> searchByUserId(@PathVariable UUID userId) {
        List<Car> cars = carService.findByUserId(userId);
        if (cars.isEmpty()) {
            log.debug("No cars found for user ID {}", userId);
            return ResponseEntity.noContent().build();
        }
        List<CarDTO> carDTOs = mapper.toCarDTOs(cars);
        log.debug("Retrieved cars for user ID {}", userId);
        return new ResponseEntity<>(carDTOs, HttpStatus.OK);
    }
}

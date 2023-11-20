package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("chpman/car")
@RequiredArgsConstructor
@Tag(name = "Car (Машина)", description = "Для обработки списка машин")
@ApiResponses(value = {@ApiResponse(responseCode = "401",
        description = "Произошла ошибка, Нужно авторизоваться")})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class CarController {

    private final CarMapper mapper;
    private final CarService carService;
    private final CarBrandService carBrandService;

    @Operation(summary = "Добавить новый автомобиль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Автомобиль успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
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

    @Operation(summary = "Обновить данные автомобиля")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные автомобиля успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
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

    @Operation(summary = "Удалить автомобиль по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Автомобиль успешно удален"),
            @ApiResponse(responseCode = "404", description = "Нет такого Автомобиля по этому id"),
    })
    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
        if (!carService.existsById(carId)) {
            log.warn("Attempt to delete non-existing car with ID {}", carId);
            return ResponseEntity.notFound().build();
        }
        carService.deleteCar(carId);
        log.info("Car with ID {} deleted", carId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить список всех автомобилей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список автомобилей найден"),
            @ApiResponse(responseCode = "204", description = "Автомобили не найдены"),
    })
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

    @Operation(summary = "Найти машины из пропусков пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращен список машин"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
    })
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

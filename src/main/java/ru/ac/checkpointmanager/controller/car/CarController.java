package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;
import ru.ac.checkpointmanager.service.car.CarService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("chpman/car")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;

    @Operation(summary = "Добавить новый автомобиль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Автомобиль успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Нужно авторизоваться")
    })
    @PostMapping
    public ResponseEntity<?> addCar(@Valid @RequestBody CarDTO carDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        Car car = Mapper.toCar(carDTO);

        try {
            CarBrand existingBrand = carBrandService.getBrandById(carDTO.getBrand().getId());
            carDTO.setBrand(existingBrand);
            Car newCar = carService.addCar(car);
            return new ResponseEntity<>(Mapper.toCarDTO(newCar), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Обновить данные автомобиля")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные автомобиля успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Нужно авторизоваться")
    })
    @PutMapping("/{carId}")
    public ResponseEntity<?> updateCar(@Valid @PathVariable UUID carId, @RequestBody CarDTO updateCarDto, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        Car car = Mapper.toCar(updateCarDto);

        Car updated =  carService.updateCar(carId, car);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить автомобиль по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Автомобиль успешно удален"),
            @ApiResponse(responseCode = "404", description = "Нет такого Автомобиля по этому id"),
            @ApiResponse(responseCode = "401", description = "Нужно авторизоваться")
    })
    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deletedCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить список всех автомобилей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список автомобилей найден"),
            @ApiResponse(responseCode = "204", description = "Автомобили не найдены"),
            @ApiResponse(responseCode = "401", description = "Нужно авторизоваться")
    })
    @GetMapping
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<Car> carList = carService.getAllCars();
        if (carList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(Mapper.toCarDTO(carList));
    }
}

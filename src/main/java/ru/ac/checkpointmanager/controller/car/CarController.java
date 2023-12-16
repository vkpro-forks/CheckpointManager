package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.mapper.CarMapper;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.service.car.CarService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/car")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Car (Машина)", description = "Для обработки списка машин")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})

public class CarController {

    private final CarMapper mapper;
    private final CarService carService;

    @Operation(summary = "Добавить новую машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Машина успешно добавлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarDTO addCar(@Valid @RequestBody CarDTO carDTO) {
        Car newCar = carService.addCar(mapper.toCar(carDTO));
        log.info("New car added: {}", newCar);
        return mapper.toCarDTO(newCar);
    }

    @Operation(summary = "Обновить новую машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Машина успешно обновлена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
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
            @ApiResponse(responseCode = "204", description = "Машина успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Такой машины не существует.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletedCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
    }

    @Operation(summary = "Получение всех машин.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список машин получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarDTO.class))}),
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
                            schema = @Schema(implementation = CarDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CarDTO>> searchByUserId(@PathVariable UUID userId) {
        List<Car> cars = carService.findByUserId(userId);
        List<CarDTO> carDTOs = mapper.toCarDTOs(cars);
        log.debug("Retrieved cars for user ID {}", userId);
        return new ResponseEntity<>(carDTOs, HttpStatus.OK);
    }

    @Operation(summary = "Найти машину по номеру телефона.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Машина успешно найдена. " +
                    "Может вернуть пустой список если машины не найдены.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CarDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/phone")
    public List<CarDTO> searchByPhone(@RequestParam @NotBlank String phone) {
        List<Car> cars = carService.findByPhonePart(phone);
        log.debug("Cars found with phone part: {}", phone);
        return mapper.toCarDTOs(cars);
    }

}

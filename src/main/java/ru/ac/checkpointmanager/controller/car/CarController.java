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
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.mapper.CarMapper;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.service.car.CarService;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.Constants.*;

@Slf4j
@RestController
@RequestMapping("api/v1/car")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Car (Машина)", description = "Для обработки списка машин")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = UNAUTHORIZED_MESSAGE,
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR,
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))})

public class CarController {

    private final CarMapper mapper;
    private final CarService carService;

    @Operation(summary = "Добавить новую машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Машина успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarDTO.class))}),
            @ApiResponse(responseCode = "400", description = FAILED_FIELD_VALIDATION_MESSAGE,
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
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarDTO.class))}),
            @ApiResponse(responseCode = "400", description = FAILED_FIELD_VALIDATION_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/{carId}")
    public Car updateCar(@PathVariable UUID carId,
                         @Valid @RequestBody CarDTO updateCarDto) {
        return carService.updateCar(carId, updateCarDto);
    }

    @Operation(summary = "Удалить машину",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Машина успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Такой машины не существует.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable UUID carId) {
        carService.deleteCar(carId);
    }

    @Operation(summary = "Получение всех машин.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список машин получен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping
    public List<CarDTO> getAllCars() {
        List<Car> carList = carService.getAllCars();
        return mapper.toCarDTOs(carList);
    }

    @Operation(summary = "Получение всех машин по user.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список машин получен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public List<CarDTO> searchByUserId(@PathVariable UUID userId) {
        List<Car> cars = carService.findByUserId(userId);
        return mapper.toCarDTOs(cars);
    }

    @Operation(summary = "Найти машину по номеру телефона.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Машина успешно найдена. " +
                    "Может вернуть пустой список если машины не найдены.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/phone")
    public List<CarDTO> searchByPhone(@RequestParam @NotBlank String phone) {
        List<Car> cars = carService.findByPhonePart(phone);
        return mapper.toCarDTOs(cars);
    }

}

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

import static ru.ac.checkpointmanager.utils.Constants.ACCESS_ADMIN_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.ACCESS_ALL_ROLES_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.ADD_CAR_BRAND_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.BRAND_NOT_EXIST_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.BRAND_PROCESSING_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.CAR_BRANDS_LIST_RECEIVED_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.CAR_BRAND_ADDED_SUCCESS_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.CAR_BRAND_DELETED_SUCCESS_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.CAR_BRAND_RECEIVED_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.CAR_BRAND_UPDATED_SUCCESS_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.DELETE_CAR_BRAND_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.FAILED_FIELD_VALIDATION_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.GET_ALL_CAR_BRANDS_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.GET_CAR_BRAND_BY_ID_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.GET_CAR_BRAND_BY_NAME_PART_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.INTERNAL_SERVER_ERROR;
import static ru.ac.checkpointmanager.utils.Constants.UNAUTHORIZED_MESSAGE;
import static ru.ac.checkpointmanager.utils.Constants.UPDATE_CAR_BRAND_MESSAGE;

@Slf4j
@RestController
@RequestMapping("api/v1/car")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CarBrand (Бренд Машины)", description = BRAND_PROCESSING_MESSAGE)
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = UNAUTHORIZED_MESSAGE),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR)})
public class CarBrandController {

    private final CarBrandService carBrandService;

    @Operation(summary = ADD_CAR_BRAND_MESSAGE,
            description = ACCESS_ADMIN_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = CAR_BRAND_ADDED_SUCCESS_MESSAGE,
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "400", description = FAILED_FIELD_VALIDATION_MESSAGE)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public CarBrand createBrand(@Valid @RequestBody CarBrandDTO brand) {
        return carBrandService.addBrand(brand);
    }

    @Operation(summary = GET_CAR_BRAND_BY_ID_MESSAGE,
            description = ACCESS_ALL_ROLES_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = CAR_BRAND_RECEIVED_MESSAGE,
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "404", description = BRAND_NOT_EXIST_MESSAGE)
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/brands/{id}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long id) {
        CarBrand brand = carBrandService.getBrandById(id);
        log.debug("Retrieved CarBrand by ID: {}", brand);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }


    @Operation(summary = DELETE_CAR_BRAND_MESSAGE,
            description = ACCESS_ADMIN_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = CAR_BRAND_DELETED_SUCCESS_MESSAGE,
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "404", description = BRAND_NOT_EXIST_MESSAGE)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/brands/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCarBrandById(@PathVariable Long id) {
        carBrandService.deleteBrand(id);
    }

    @Operation(summary = UPDATE_CAR_BRAND_MESSAGE,
            description = ACCESS_ADMIN_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = CAR_BRAND_UPDATED_SUCCESS_MESSAGE,
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
            @ApiResponse(responseCode = "400", description = FAILED_FIELD_VALIDATION_MESSAGE)
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/brands/{id}")
    public CarBrand updateCarBrand(@Valid @PathVariable Long id,
                                   @Valid @RequestBody CarBrandDTO carBrandDetails) {
        return carBrandService.updateBrand(id, carBrandDetails);
    }

    @Operation(summary = GET_ALL_CAR_BRANDS_MESSAGE,
            description = ACCESS_ALL_ROLES_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = CAR_BRANDS_LIST_RECEIVED_MESSAGE,
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

    @Operation(summary = GET_CAR_BRAND_BY_NAME_PART_MESSAGE,
            description = ACCESS_ALL_ROLES_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = CAR_BRANDS_LIST_RECEIVED_MESSAGE,
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

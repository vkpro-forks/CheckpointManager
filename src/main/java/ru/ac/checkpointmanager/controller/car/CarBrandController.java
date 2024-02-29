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
import ru.ac.checkpointmanager.annotation.PreAuthorizeAllRoles;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.car.CarBrandService;

import java.util.List;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.ACCESS_ADMIN_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.ACCESS_ALL_ROLES_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.ADD_CAR_BRAND_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.BRAND_NOT_EXIST_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.BRAND_PROCESSING_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.CAR_BRANDS_LIST_RECEIVED_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.CAR_BRAND_ADDED_SUCCESS_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.CAR_BRAND_DELETED_SUCCESS_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.CAR_BRAND_RECEIVED_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.CAR_BRAND_UPDATED_SUCCESS_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.DELETE_CAR_BRAND_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.FAILED_FIELD_VALIDATION_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.GET_ALL_CAR_BRANDS_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.GET_CAR_BRAND_BY_ID_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.GET_CAR_BRAND_BY_NAME_PART_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UPDATE_CAR_BRAND_MESSAGE;

@Slf4j
@RestController
@RequestMapping("api/v1/cars/brands")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "CarBrand (марки машин)", description = BRAND_PROCESSING_MESSAGE)
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = UNAUTHORIZED_MSG),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR_MSG)})
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
    @PostMapping()
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
    @PreAuthorizeAllRoles
    @GetMapping("/{brandId}")
    public ResponseEntity<CarBrand> getCarBrandById(@PathVariable Long brandId) {
        CarBrand brand = carBrandService.getBrandById(brandId);
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
    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCarBrandById(@PathVariable Long brandId) {
        carBrandService.deleteBrand(brandId);
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
    @PutMapping("/{brandId}")
    public CarBrand updateCarBrand(@Valid @PathVariable Long brandId,
                                   @Valid @RequestBody CarBrandDTO carBrandDetails) {
        return carBrandService.updateBrand(brandId, carBrandDetails);
    }

    @Operation(summary = GET_ALL_CAR_BRANDS_MESSAGE,
            description = ACCESS_ALL_ROLES_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = CAR_BRANDS_LIST_RECEIVED_MESSAGE,
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CarBrand.class))}),
    })
    @PreAuthorizeAllRoles
    @GetMapping()
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
    @PreAuthorizeAllRoles
    @GetMapping("/name")
    public ResponseEntity<List<CarBrand>> getBrandsByName(@RequestParam String brandNamePart) {
        List<CarBrand> brands = carBrandService.findByBrandsContainingIgnoreCase(brandNamePart);
        log.debug("Retrieved CarBrands by name part: {}", brands);
        return new ResponseEntity<>(brands, HttpStatus.OK);
    }
}

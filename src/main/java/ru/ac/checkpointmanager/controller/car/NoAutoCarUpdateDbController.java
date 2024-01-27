package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.service.car.CarDataUpdater;

@RestController
@RequestMapping("api/v1/car")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class NoAutoCarUpdateDbController {

    private final CarDataUpdater carDataUpdater;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/update-data")
    public void updateCarDataManually(@RequestParam(defaultValue = "10") String limit,
                                      @RequestParam(defaultValue = "0") String page,
                                      @RequestParam(required = false) String brand,
                                      @RequestParam(required = false) String model) {
        if (brand != null) {
            carDataUpdater.updateCarDataFromAPIWithBrandAndModel(limit, page, brand, model);
        } else {
            carDataUpdater.updateCarDataFromAPI(limit, page);
        }
    }

}

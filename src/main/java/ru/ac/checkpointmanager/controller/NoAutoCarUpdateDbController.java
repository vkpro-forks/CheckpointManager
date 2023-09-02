package ru.ac.checkpointmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarModelRepository;
import ru.ac.checkpointmanager.service.car.CarDataUpdater;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class NoAutoCarUpdateDbController {

    private final CarDataUpdater carDataUpdater;

    @GetMapping("/update-data")
    public ResponseEntity<String> updateCarDataManually(@RequestParam(defaultValue = "10") String limit,
                                                        @RequestParam(defaultValue = "0") String page,
                                                        @RequestParam(required = false) String brand,
                                                        @RequestParam(required = false) String model
    ) {

        List<String> updatedBrands = new ArrayList<>();
        List<String> updatedModels;

        System.out.println("Brand: " + brand);
        System.out.println("Model: " + model);

        if (brand != null) {
            // Формируем запрос с указанным брендом и моделью
            // Вызываем метод обновления данных с указанными параметрами
            updatedModels = carDataUpdater.updateCarDataFromAPIWithBrandAndModel(limit, page, brand, model);
            updatedBrands.add(brand);
        } else {
            // Обычный вызов метода обновления данных без указания бренда и модели
            updatedModels = carDataUpdater.updateCarDataFromAPI(limit, page);
        }

        String responseMessage;
        if (!updatedModels.isEmpty()) {
            responseMessage = "Data update triggered successfully. " +
                    "Updated " + updatedModels.size() + " models: " +
                    String.join(", ", updatedModels);
            return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
        } else {
            return ResponseEntity.noContent().build();
        }

    }

}

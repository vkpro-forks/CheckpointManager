package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.repository.CarBrandRepository;
import ru.ac.checkpointmanager.repository.CarModelRepository;

import java.util.List;

public interface CarDataUpdater {
    List<String> updateCarDataFromAPI(String limit, String page);

    List<String> updateCarDataFromAPIWithBrandAndModel(String limit, String page, String brand, String model);

}

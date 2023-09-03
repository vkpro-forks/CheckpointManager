package ru.ac.checkpointmanager.service.car;

import java.util.List;

public interface CarDataUpdater {
    List<String> updateCarDataFromAPI(String limit, String page);

    List<String> updateCarDataFromAPIWithBrandAndModel(String limit, String page, String brand, String model);

}

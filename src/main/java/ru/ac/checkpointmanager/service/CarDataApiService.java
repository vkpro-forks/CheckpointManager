package ru.ac.checkpointmanager.service;

import java.util.List;

public interface CarDataApiService {
    public String fetchCarData(String limit, String page);

    String fetchCarDataWithBrandAndModel(String limit, String page, String brand, String model);
}

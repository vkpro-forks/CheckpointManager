package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;

public interface CarModelService {
    CarModel getModelById(Long brandId);

    CarModel addModel(CarModel carModel);

    void deleteModel(Long brandId);

    CarModel updateModel(Long brandId, CarModel carModel);
}

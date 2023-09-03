package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.model.car.CarModel;

import java.util.List;

public interface CarModelService {
    CarModel getModelById(Long brandId);

    CarModel addModel(CarModel carModel);

    void deleteModel(Long brandId);

    CarModel updateModel(Long brandId, CarModel carModel);

    List<CarModel> getAllModels();

    CarModel findByModelContainingIgnoreCase(String modelNamePart);

}

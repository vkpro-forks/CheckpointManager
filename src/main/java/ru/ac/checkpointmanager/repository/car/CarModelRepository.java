package ru.ac.checkpointmanager.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.car.CarModel;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    CarModel findByModel(String modelName);


    CarModel findByModelContainingIgnoreCase(String modelName);
}

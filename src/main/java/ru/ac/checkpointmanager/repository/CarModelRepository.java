package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    CarModel findByModel(String modelName);

    boolean existsByBrandAndModel(CarBrand brand, String model);

}

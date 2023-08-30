package ru.ac.checkpointmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    CarModel findByModel(String modelName);

    boolean existsByBrandAndModel(CarBrand brand, String model);
    CarModel findByModelContainingIgnoreCase(String modelName);
}

package ru.ac.checkpointmanager.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.List;

public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {

    List<CarBrand> findByBrandContainingIgnoreCase(String brandName);

    CarBrand findByBrand(String name);
}

package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.CarBrand;

public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {
}

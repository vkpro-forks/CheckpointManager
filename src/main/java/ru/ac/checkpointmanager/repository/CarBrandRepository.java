package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.CarBrand;

import java.util.List;

public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {

    List<CarBrand> findByNameIgnoreCase(String name);
}

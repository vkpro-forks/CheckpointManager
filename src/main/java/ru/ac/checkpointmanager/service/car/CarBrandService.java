package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.List;

public interface CarBrandService {
    CarBrand getBrandById(Long brandId);

    CarBrand addBrand(CarBrandDTO carBrand);

    void deleteBrand(Long brandId);

    CarBrand updateBrand(Long brandId, CarBrandDTO carBrand);

    List<CarBrand> getAllBrands();

    List<CarBrand> findByBrandsContainingIgnoreCase(String brandName);
}

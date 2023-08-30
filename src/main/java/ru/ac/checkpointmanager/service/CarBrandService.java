package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;

import java.util.List;

public interface CarBrandService {
    CarBrand getBrandById(Long brandId);

    CarBrand addBrand(CarBrand brand);

    void deleteBrand(Long brandId);

    CarBrand updateBrand(Long brandId, CarBrand carBrand);

    List<CarBrand> getAllBrands();
    CarBrand findByBrandsContainingIgnoreCase(String brandName);

    List<CarModel> findModelsByBrandId(Long brandId);
}

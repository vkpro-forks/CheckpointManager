package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.CarBrand;

import java.util.List;

public interface CarBrandService {
    CarBrand getBrandById(Long brandId);

    CarBrand addBrand(CarBrand brand);

    void deleteBrand(Long brandId);

    CarBrand updateBrand(Long brandId, CarBrand carBrand);
    List<CarBrand> findByBrandIgnoreCase(String name);

}

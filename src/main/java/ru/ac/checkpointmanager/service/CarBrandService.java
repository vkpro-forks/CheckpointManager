package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.CarBrand;

public interface CarBrandService {
    CarBrand getBrandById(Long brandId);

    CarBrand addBrand(CarBrand brand);

    void deleteBrand(Long brandId);

    CarBrand updateBrand(Long brandId, CarBrand carBrand);

}

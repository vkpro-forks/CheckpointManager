package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.model.car.CarBrand;

import java.util.List;

public interface CarBrandService {
    CarBrand getBrandById(Long brandId);

    CarBrand addBrand(CarBrand brand);

    void deleteBrand(Long brandId);

    CarBrand updateBrand(Long brandId, CarBrand carBrand);

    List<CarBrand> getAllBrands();

    List<CarBrand> findByBrandsContainingIgnoreCase(String brandName);

    boolean existsById(Long id);
}

package ru.ac.checkpointmanager.service.car;

import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.car.CarModel;

import java.util.List;

public interface CarBrandService {
    CarBrand getBrandById(Long brandId);

    CarBrand addBrand(CarBrand brand);

    void deleteBrand(Long brandId);

    void deleteBrandAndAllModelsByBrand(Long brandId);

    CarBrand updateBrand(Long brandId, CarBrand carBrand);

    List<CarBrand> getAllBrands();

    List<CarBrand> findByBrandsContainingIgnoreCase(String brandName);

    List<CarModel> findModelsByBrandId(Long brandId);
}

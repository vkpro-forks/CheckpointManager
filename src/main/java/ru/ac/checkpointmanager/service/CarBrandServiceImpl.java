package ru.ac.checkpointmanager.service;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.repository.CarBrandRepository;

@Service
public class CarBrandServiceImpl implements CarBrandService {

    private final CarBrandRepository carBrandRepository;

    public CarBrandServiceImpl(CarBrandRepository carBrandRepository) {
        this.carBrandRepository = carBrandRepository;
    }

    @Override
    public CarBrand getBrandById(Long id) {
        return carBrandRepository.findById(id)
                .orElseThrow(()-> new CarBrandNotFoundException("Car brand not found with ID: " + id));
    }

    @Override
    public CarBrand addBrand(CarBrand brand) {
        return carBrandRepository.save(brand);
    }

    @Override
    public void deleteBrand(Long brandId) {
        carBrandRepository.findById(brandId)
                .orElseThrow(()-> new CarBrandNotFoundException("Car brand not found with ID: " + brandId));
    }

    @Override
    public CarBrand updateBrand(Long brandId, CarBrand carBrand) {
        CarBrand requestBrand = carBrandRepository.findById(brandId)
                .orElseThrow(()-> new CarBrandNotFoundException("Car brand not found with ID: " + brandId));
        requestBrand.setBrand(carBrand.getBrand());
        return carBrandRepository.save(requestBrand);
    }
}

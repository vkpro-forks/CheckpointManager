package ru.ac.checkpointmanager.service;

import org.springframework.stereotype.Service;
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
        return carBrandRepository.findById(id).orElseThrow(null);
    }
}

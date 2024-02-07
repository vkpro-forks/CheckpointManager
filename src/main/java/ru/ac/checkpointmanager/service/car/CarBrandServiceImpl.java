package ru.ac.checkpointmanager.service.car;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.exception.CarBrandAlreadyExistsException;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarBrandServiceImpl implements CarBrandService {
    private final CarBrandRepository carBrandRepository;

    @Override
    public CarBrand getBrandById(Long brandId) {
        CarBrand carBrand = carBrandRepository.findById(brandId).orElseThrow(() -> {
            String errorMsg = ExceptionUtils.CAR_BRAND_NOT_FOUND_ID.formatted(brandId);
            log.warn(errorMsg);
            return new CarBrandNotFoundException(errorMsg);
        });
        log.debug("Car brand with [brandId: {}] retrieved from repository", brandId);
        return carBrand;
    }

    @Override
    public CarBrand addBrand(CarBrandDTO carBrand) {
        if (carBrandRepository.existsByBrand(carBrand.getBrand())) {
            log.warn(ExceptionUtils.CAR_BRAND_EXISTS.formatted(carBrand.getBrand()));
            throw new CarBrandAlreadyExistsException(ExceptionUtils.CAR_BRAND_EXISTS.formatted(carBrand.getBrand()));
        }
        CarBrand carBrandEntity = new CarBrand();
        carBrandEntity.setBrand(carBrand.getBrand());
        return carBrandRepository.save(carBrandEntity);
    }

    @Override
    public void deleteBrand(Long brandId) {
        if (!carBrandRepository.existsById(brandId)) {
            String errorMsg = ExceptionUtils.CAR_BRAND_NOT_FOUND_ID.formatted(brandId);
            log.warn(errorMsg);
            throw new CarBrandNotFoundException(errorMsg);
        }
        carBrandRepository.deleteById(brandId);
        log.info("Car brand with [id: {}] successfully deleted", brandId);
    }

    @Override
    public CarBrand updateBrand(Long brandId, CarBrandDTO carBrand) {
        CarBrand updateCarBrand = carBrandRepository.findById(brandId).orElseThrow(() -> {
            String errorMsg = ExceptionUtils.CAR_BRAND_NOT_FOUND_ID.formatted(brandId);
            log.warn(errorMsg);
            return new CarBrandNotFoundException(errorMsg);
        });
        if (carBrandRepository.existsByBrand(carBrand.getBrand())) {
            log.warn(ExceptionUtils.CAR_BRAND_EXISTS.formatted(carBrand.getBrand()));
            throw new CarBrandAlreadyExistsException(ExceptionUtils.CAR_BRAND_EXISTS.formatted(carBrand.getBrand()));
        }
        updateCarBrand.setBrand(carBrand.getBrand());
        CarBrand saved = carBrandRepository.save(updateCarBrand);
        log.info("Car brand with [id: {}] successfully updated", brandId);
        return saved;
    }

    @Override
    public List<CarBrand> getAllBrands() {
        return carBrandRepository.findAll();
    }

    @Override
    public List<CarBrand> findByBrandsContainingIgnoreCase(String brandName) {
        return carBrandRepository.findByBrandContainingIgnoreCase(brandName);
    }
}

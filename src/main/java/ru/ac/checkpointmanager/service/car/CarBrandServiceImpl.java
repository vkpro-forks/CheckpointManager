package ru.ac.checkpointmanager.service.car;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarBrandServiceImpl implements CarBrandService {

    public static final String CAR_BRAND_NOT_FOUND_WITH_ID_MSG = "Car brand not found with ID:";

    private final CarBrandRepository carBrandRepository;
    private final Validator validator;

    @Override
    public CarBrand getBrandById(Long brandId) {
        CarBrand carBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> {
                    log.warn(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " {}", brandId);
                    return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " " + brandId);
                });
        log.debug("Car brand with [brandId: {}] retrieved from repository", brandId);
        return carBrand;
    }


    @Override
    public CarBrand addBrand(CarBrand brand) {
        CarBrand existingBrand = carBrandRepository.findByBrand(brand.getBrand());
        if (existingBrand != null) {
            throw new IllegalArgumentException("A brand with the same name already exists!");
        }
        return carBrandRepository.save(brand);
    }


    //удалить бренд можно только в том случае, если у этого бренда в бд нет ни одной модели
    @Override
    public void deleteBrand(Long brandId) {
        CarBrand carBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> {
                    log.warn(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " {}", brandId);
                    return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " " + brandId);
                });
        carBrandRepository.deleteById(brandId);
        log.info("Car brand with [id: {}] successfully deleted", brandId);
    }


    @Override
    public CarBrand updateBrand(Long brandId, CarBrand carBrand) {
        CarBrand updateCarBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> {
                    log.warn(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " {}", brandId);
                    return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " " + brandId);
                });
        Set<ConstraintViolation<CarBrand>> violations = validator.validate(carBrand);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations); //TODO will be moved to global validation
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

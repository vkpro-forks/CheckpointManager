package ru.ac.checkpointmanager.service.car;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.exception.CarBrandAlreadyExistsException;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarBrandServiceImpl implements CarBrandService {

    public static final String CAR_BRAND_NOT_FOUND_WITH_ID_MSG = "Car brand not found with ID:";
    public static final String CAR_BRAND_EXISTS = "CarBrand with [name: %s] already exists";

    private final CarBrandRepository carBrandRepository;

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
    public CarBrand addBrand(CarBrandDTO carBrand) {
        Optional<CarBrand> carBrandOptional = carBrandRepository.findByBrand(carBrand.getBrand());
        if (carBrandOptional.isPresent()) {
            log.warn(CAR_BRAND_EXISTS.formatted(carBrand.getBrand()));
            throw new CarBrandAlreadyExistsException(CAR_BRAND_EXISTS.formatted(carBrand.getBrand()));
        }
        CarBrand carBrandEntity = new CarBrand();
        carBrandEntity.setBrand(carBrand.getBrand());
        return carBrandRepository.save(carBrandEntity);
    }


    //удалить бренд можно только в том случае, если у этого бренда в бд нет ни одной модели
    @Override
    public void deleteBrand(Long brandId) {
        CarBrand carBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> {
                    log.warn(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " {}", brandId);
                    return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " " + brandId);
                });
        carBrandRepository.delete(carBrand);
        log.info("Car brand with [id: {}] successfully deleted", brandId);
    }


    @Override
    public CarBrand updateBrand(Long brandId, CarBrandDTO carBrand) {
        CarBrand updateCarBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> {
                    log.warn(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " {}", brandId);
                    return new CarBrandNotFoundException(CAR_BRAND_NOT_FOUND_WITH_ID_MSG + " " + brandId);
                });
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

    @Override
    public boolean existsById(Long id) {
        return carBrandRepository.existsById(id);
    }

}

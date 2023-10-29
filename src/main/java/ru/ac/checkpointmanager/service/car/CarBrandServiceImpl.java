package ru.ac.checkpointmanager.service.car;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarBrandServiceImpl implements CarBrandService {

    private final CarBrandRepository carBrandRepository;
        private final Validator validator;

    @Override
    public CarBrand getBrandById(Long id) {
        return carBrandRepository.findById(id)
                .orElseThrow(() -> new CarBrandNotFoundException("Car brand not found with ID: " + id));
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
                .orElseThrow(() -> new CarBrandNotFoundException("Car brand not found with ID: " + brandId));

        carBrandRepository.deleteById(brandId);
    }

    //удаляем бренд и все модели которые к нему привязаны
    @Override
    public void deleteBrandAndAllModelsByBrand(Long brandId) {
        CarBrand carBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> new CarBrandNotFoundException("Car brand not found with ID: " + brandId));

        carBrandRepository.deleteById(brandId);
    }

    @Override
    public CarBrand updateBrand(Long brandId, CarBrand carBrand) {
        CarBrand updateCarBrand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> new CarBrandNotFoundException("Brand not found with ID" + brandId));

        Set<ConstraintViolation<CarBrand>> violations = validator.validate(carBrand);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        updateCarBrand.setBrand(carBrand.getBrand());
        return carBrandRepository.save(updateCarBrand);
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

package ru.ac.checkpointmanager.service.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.service.car.CarBrandServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarBrandServiceImplTest {
    @Mock
    private CarBrandRepository carBrandRepository;

    @InjectMocks
    private CarBrandServiceImpl carBrandService;

    @Test
    void getBrandByIdWhenBrandExists() {
        CarBrand carBrand = new CarBrand();
        carBrand.setId(1L);
        when(carBrandRepository.findById(1L)).thenReturn(Optional.of(carBrand));

        CarBrand result = carBrandService.getBrandById(1L);

        assertEquals(carBrand, result);
    }

    @Test
    void getBrandByIdWhenBrandDoesNotExist() {
        when(carBrandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarBrandNotFoundException.class, () -> carBrandService.getBrandById(1L));
    }

    @Test
    void addBrand() {
        CarBrand carBrand = new CarBrand();
        when(carBrandRepository.save(carBrand)).thenReturn(carBrand);

        CarBrand result = carBrandService.addBrand(carBrand);

        assertEquals(carBrand, result);
    }

    @Test
    void deleteBrandWhenBrandExists() {
        CarBrand carBrand = new CarBrand();
        carBrand.setId(1L);
        when(carBrandRepository.findById(1L)).thenReturn(Optional.of(carBrand));

        carBrandService.deleteBrand(1L);

        verify(carBrandRepository).deleteById(1L);
    }

    @Test
    void deleteBrandWhenBrandDoesNotExist() {
        when(carBrandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarBrandNotFoundException.class, () -> carBrandService.deleteBrand(1L));
    }

    @Test
    void updateBrand() {
        CarBrand existingBrand = new CarBrand();
        existingBrand.setId(1L);
        existingBrand.setBrand("Existing Brand");

        CarBrand newBrand = new CarBrand();
        newBrand.setBrand("New Brand");

        when(carBrandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(carBrandRepository.save(existingBrand)).thenReturn(existingBrand);

        CarBrand updatedBrand = carBrandService.updateBrand(1L, newBrand);

        assertEquals("New Brand", updatedBrand.getBrand());
    }


}

package ru.ac.checkpointmanager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.ac.checkpointmanager.exception.CarNotFoundException;
import ru.ac.checkpointmanager.model.Car;
import ru.ac.checkpointmanager.repository.CarRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;
    @InjectMocks
    private CarServiceImpl carService;

    @Test
    void addCar() {
        Car car = new Car();

        when(carRepository.save(any(Car.class))).thenReturn(car);

        Car savedCar = carService.addCar(car);

        assertNotNull(savedCar);
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void deleteCarSuccess() {
        UUID carId = UUID.randomUUID();

        doNothing().when(carRepository).deleteById(carId);
        carService.deleteCar(carId);

        verify(carRepository).deleteById(carId);
    }

    @Test
    void deleteCarNotFound() {
        UUID carId = UUID.randomUUID();

        doThrow(EmptyResultDataAccessException.class).when(carRepository).deleteById(carId);

        assertThrows(CarNotFoundException.class, () -> carService.deleteCar(carId));
    }

    @Test
    void updateCar() {
        UUID carId = UUID.randomUUID();
        Car existingCar = new Car();
        Car updatedCar = new Car();

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carRepository.save(existingCar)).thenReturn(existingCar);

        Car result = carService.updateCar(carId, updatedCar);

        assertNotNull(result);
        verify(carRepository).findById(carId);
        verify(carRepository).save(existingCar);
    }

    @Test
    void getCarById() {
        UUID carId = UUID.randomUUID();
        Car car = new Car();

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        Car foundCar = carService.getCarById(carId);

        assertNotNull(foundCar);
        verify(carRepository).findById(carId);
    }

    @Test
    void getAllCars() {
        Car car1 = new Car();
        Car car2 = new Car();

        when(carRepository.findAll()).thenReturn(Arrays.asList(car1, car2));

        List<Car> cars = carService.getAllCars();

        assertNotNull(cars);
        assertEquals(2, cars.size());
        verify(carRepository).findAll();
    }

}

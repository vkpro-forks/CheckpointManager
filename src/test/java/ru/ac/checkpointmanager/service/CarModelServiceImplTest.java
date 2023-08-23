package ru.ac.checkpointmanager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.repository.CarModelRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarModelServiceImplTest {

    @Mock
    private CarModelRepository carModelRepository;

    @InjectMocks
    private CarModelServiceImpl carModelService;

    @Test
    void getModelById() {
        CarModel carModel = new CarModel();
        carModel.setId(1L);
        carModel.setModel("TestModel");

        when(carModelRepository.findById(1L)).thenReturn(Optional.of(carModel));

        CarModel foundModel = carModelService.getModelById(1L);

        assertEquals("TestModel", foundModel.getModel());
    }

    @Test
    void getModelByIdNotFound() {
        when(carModelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarModelNotFoundException.class, () -> carModelService.getModelById(1L));
    }

    @Test
    void addModel() {
        CarModel carModel = new CarModel();
        carModel.setModel("TestModel");

        when(carModelRepository.save(carModel)).thenReturn(carModel);

        CarModel savedModel = carModelService.addModel(carModel);

        assertEquals("TestModel", savedModel.getModel());
    }

    @Test
    void deleteModel() {
        CarModel carModel = new CarModel();
        carModel.setId(1L);
        carModel.setModel("TestModel");

        when(carModelRepository.findById(1L)).thenReturn(Optional.of(carModel));

        carModelService.deleteModel(1L);

        verify(carModelRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteModelNotFound() {
        when(carModelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarModelNotFoundException.class, () -> carModelService.deleteModel(1L));
    }

    @Test
    void updateModel() {
        CarModel existingModel = new CarModel();
        existingModel.setId(1L);
        existingModel.setModel("Existing Model");

        CarModel newModel = new CarModel();
        newModel.setModel("New Model");

        when(carModelRepository.findById(1L)).thenReturn(Optional.of(existingModel));
        when(carModelRepository.save(existingModel)).thenReturn(existingModel);

        CarModel updateModel = carModelService.updateModel(1L, newModel);

        assertEquals("New Model", updateModel.getModel());
    }
}

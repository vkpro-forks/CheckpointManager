package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.CarBrand;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.repository.CarModelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarModelServiceImpl implements CarModelService {

    private final CarModelRepository carModelRepository;

    @Override
    public CarModel addModel(CarModel carModel) {
        return carModelRepository.save(carModel);
    }

    @Override
    public CarModel getModelById(Long id) {
        return carModelRepository.findById(id)
                .orElseThrow(() -> new CarModelNotFoundException("Car model not found with ID: " + id));
    }

    @Override
    public void deleteModel(Long id) {
        carModelRepository.findById(id)
                .orElseThrow(() -> new CarModelNotFoundException("Car model not found with ID: " + id));
        carModelRepository.deleteById(id);
    }

    @Override
    public CarModel updateModel(Long id, CarModel carModel) {
        CarModel requestModel = carModelRepository.findById(id)
                .orElseThrow(() -> new CarModelNotFoundException("Car model not found with ID: " + id));
        requestModel.setModel(carModel.getModel());
        return carModelRepository.save(requestModel);
    }

    @Override
    public List<CarModel> getAllModels() {
        return carModelRepository.findAll();
    }

    @Override
    public CarModel findByModelContainingIgnoreCase(String modelNamePart) {
        return carModelRepository.findByModelContainingIgnoreCase(modelNamePart);
    }

}

package ru.ac.checkpointmanager.service;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CarModelNotFoundException;
import ru.ac.checkpointmanager.model.CarModel;
import ru.ac.checkpointmanager.repository.CarModelRepository;

import java.util.List;

@Service
public class CarModelServiceImpl implements CarModelService {

    private final CarModelRepository carModelRepository;

    public CarModelServiceImpl(CarModelRepository carModelRepository) {
        this.carModelRepository = carModelRepository;
    }

    @Override
    public CarModel getModelById(Long id) {
        return carModelRepository.findById(id)
                .orElseThrow(() -> new CarModelNotFoundException("Car model not found with ID: " + id));
    }

    @Override
    public CarModel addModel(CarModel carModel) {
        return carModelRepository.save(carModel);
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
    public List<CarModel> findByModelIgnoreCase(String name) {
        return carModelRepository.findByModelContainingIgnoreCase(name);
    }

}

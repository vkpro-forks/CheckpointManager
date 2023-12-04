package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.model.car.Car;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class CarMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public CarMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public Car toCar(CarDTO carDTO) {
        log.debug("Conversion from CarDTO to Car");
        return modelMapper.map(carDTO, Car.class);
    }

    public CarDTO toCarDTO(Car car) {
        log.debug("Converting from Car to CarDTO");
        return modelMapper.map(car, CarDTO.class);
    }

    public List<CarDTO> toCarDTOs(Collection<Car> cars) {
        log.debug("Converting a list of Car objects to a list of CarDTOs");
        return cars.stream()
                .map(e -> modelMapper.map(e, CarDTO.class))
                .toList();
    }
}

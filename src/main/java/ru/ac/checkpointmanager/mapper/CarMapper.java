package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.model.car.Car;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class CarMapper {

    private final ModelMapper modelMapper;


    public CarMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public Car toCar(CarDTO carDTO) {
        log.info("Conversion from CarDTO to Car");
        return modelMapper.map(carDTO, Car.class);
    }

    public CarDTO toCarDTO(Car car) {
        log.info("Converting from Car to CarDTO");
        return modelMapper.map(car, CarDTO.class);
    }

    public List<CarDTO> toCarDTOs(Collection<Car> cars) {
        log.info("Converting a list of Car objects to a list of CarDTOs");
        return cars.stream()
                .map(e -> modelMapper.map(e, CarDTO.class))
                .toList();
    }
}

package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.*;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class Mapper {

    private final ModelMapper modelMapper = new ModelMapper();
    private final PassRepository passRepository;
    private final CheckpointRepository checkpointRepository;

    /* Checkpoint mapping */
    public Checkpoint toCheckpoint(CheckpointDTO checkpointDTO) {
        return modelMapper.map(checkpointDTO, Checkpoint.class);
    }

    public CheckpointDTO toCheckpointDTO(Checkpoint checkpoint) {
        return modelMapper.map(checkpoint, CheckpointDTO.class);
    }

    public List<CheckpointDTO> toCheckpointsDTO(List<Checkpoint> checkpoints) {
        return checkpoints.stream()
                .map(e -> modelMapper.map(e, CheckpointDTO.class))
                .toList();
    }

    /* Territory mapping */
    public Territory toTerritory(TerritoryDTO territoryDTO) {
        return modelMapper.map(territoryDTO, Territory.class);
    }

    public TerritoryDTO toTerritoryDTO(Territory territory) {
        return modelMapper.map(territory, TerritoryDTO.class);
    }

    public List<TerritoryDTO> toTerritoriesDTO(List<Territory> territories) {
        return territories.stream()
                .map(e -> modelMapper.map(e, TerritoryDTO.class))
                .toList();
    }

    public List<Territory> toTerritories(List<TerritoryDTO> territoriesDTO) {
        return territoriesDTO.stream()
                .map(e -> modelMapper.map(e, Territory.class))
                .toList();
    }

    /* Pass mapping */
    public Pass toPass(PassDTOin passDTOin) {
        if (passDTOin.getCar() != null) {
            return modelMapper.map(passDTOin, PassAuto.class);
        } else if (passDTOin.getPerson() != null) {
            return modelMapper.map(passDTOin, PassWalk.class);
        }
        throw new IllegalArgumentException("Ошибка при конвертации passDTO (не содержит car или person)");
    }

    public PassDTOout toPassDTO(Pass pass) {
        return modelMapper.map(pass, PassDTOout.class);
    }

    public List<PassDTOout> toPassDTO(List<Pass> pass) {
        return pass.stream()
                .map(e -> modelMapper.map(e, PassDTOout.class))
                .toList();
    }

    /* User mapping */
    public User toUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public UserDTO toUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public List<UserDTO> toUsersDTO(Collection<User> users) {
        return users.stream()
                .map(e -> modelMapper.map(e, UserDTO.class))
                .toList();
    }

    public Car toCar(CarDTO carDTO) {
        return modelMapper.map(carDTO, Car.class);
    }

    public CarDTO toCarDTO(Car car) {
        return modelMapper.map(car, CarDTO.class);
    }

    public List<CarDTO> toCarDTO(Collection<Car> cars) {
        return cars.stream()
                .map(e -> modelMapper.map(e, CarDTO.class))
                .toList();
    }

    public Person toPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }

    public PersonDTO toPersonDTO(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }

    public List<PersonDTO> toPersonDTO(Collection<Person> people) {
        return people.stream()
                .map(e -> modelMapper.map(e, PersonDTO.class))
                .toList();
    }

    public User toUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, User.class);
    }

    public UserAuthDTO toUserAuthDTO(User user) {
        return modelMapper.map(user, UserAuthDTO.class);
    }

    /**
     * Метод для конвертации временного пользователя в основного.
     * При маппинге игнорируется поле id, так как идентификатор основного пользователя назначается при сохранении в базу данных.
     *
     * @param temporaryUser временный пользователь, который будет конвертирован в основного пользователя.
     * @return User - основной пользователь.
     * @see TemporaryUser
     * @see User
     */
    public User toUser(TemporaryUser temporaryUser) {
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<TemporaryUser, User> propertyMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                skip(destination.getAddedAt());
            }
        };
        modelMapper.addMappings(propertyMap);
        try {
            User user = modelMapper.map(temporaryUser, User.class);
            log.info("Конвертация в основного пользователя прошла успешно");
            return user;
        } catch (MappingException e) {
            log.error("Ошибка при конвертации TemporaryUser в User", e);
            throw new RuntimeException("Ошибка при конвертации TemporaryUser в User", e);
        }
    }

    public TemporaryUser toTemporaryUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, TemporaryUser.class);
    }

    public LoginResponse toLoginResponse(User user) {
        return modelMapper.map(user, LoginResponse.class);
    }

    /* Phone mapping */
    public Phone toPhone(PhoneDTO phoneDTO) {
        return modelMapper.map(phoneDTO, Phone.class);
    }

    public PhoneDTO toPhoneDTO(Phone phone) {
        return modelMapper.map(phone, PhoneDTO.class);
    }

    public List<PhoneDTO> toPhonesDTO(Collection<Phone> phones) {
        return phones.stream()
                .map(p -> modelMapper.map(p, PhoneDTO.class))
                .toList();
    }

    /* Crossing mapping */
    public Crossing toCrossing(CrossingDTO crossingDTO) {
        Crossing crossing = new Crossing();
        Optional<Pass> optionalPass = passRepository.findById(crossingDTO.getPassId());
        Pass pass = optionalPass.orElseThrow(
                () -> new PassNotFoundException("Pass not found for ID " + crossingDTO.getPassId()));

        Optional<Checkpoint> optionalCheckpoint = checkpointRepository.findById(crossingDTO.getCheckpointId());
        Checkpoint checkpoint = optionalCheckpoint.orElseThrow(
                () -> new CheckpointNotFoundException("Checkpoint not found for ID " + crossingDTO.getCheckpointId()));

        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);
        crossing.setDirection(crossingDTO.getDirection());

        return crossing;
    }

    public CrossingDTO toCrossingDTO(Crossing crossing) {
        return modelMapper.map(crossing, CrossingDTO.class);
    }

    public List<CrossingDTO> toCrossingsDTO(Collection<Crossing> crossings) {
        return crossings.stream()
                .map(crossing -> modelMapper.map(crossing, CrossingDTO.class))
                .toList();
    }

}

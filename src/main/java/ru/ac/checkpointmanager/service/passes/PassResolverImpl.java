package ru.ac.checkpointmanager.service.passes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.exception.ExceptionMessage;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PassResolverImpl implements PassResolver {

    private final TerritoryRepository territoryRepository;

    private final UserRepository userRepository;

    private final CarBrandRepository carBrandRepository;

    private final PassMapper passMapper;

    /**
     * Исходя из содержимого ДТО определяется тип пропуска, и какими связанными сущностями его необходимо
     * наполнить;
     * Если бренд авто не найден, вместо ошибки создается новый
     *
     * @param passCreateDTO ДТО содержащее данные о пропуске
     * @return {@link Pass} пропуск с необходимыми данными для дальнейшей обработки
     * @throws UserNotFoundException      если нет юзера по указанному id
     * @throws TerritoryNotFoundException если нет территории по указанному id
     */
    @Override
    public Pass createPass(PassCreateDTO passCreateDTO) {
        UUID userId = passCreateDTO.getUserId();
        UUID territoryId = passCreateDTO.getTerritoryId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(ExceptionMessage.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND_MSG.formatted(userId));
                });
        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(
                        () -> {
                            log.warn(ExceptionMessage.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                            return new TerritoryNotFoundException(ExceptionMessage
                                    .TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                        });
        Pass pass;
        if (passCreateDTO.getCar() != null) {
            CarBrandDTO brand = passCreateDTO.getCar().getBrand();
            CarBrand carBrand = carBrandRepository.findByBrand(brand.getBrand()).orElse(new CarBrand(brand.getBrand()));
            PassAuto passAuto = passMapper.toPassAuto(passCreateDTO);
            Car car = passAuto.getCar();
            car.setBrand(carBrand);
            if (car.getId() == null) {
                car.setId(UUID.randomUUID());
            }
            pass = passAuto;
        } else {
            pass = passMapper.toPassWalk(passCreateDTO);
        }
        pass.setUser(user);
        pass.setTerritory(territory);
        return pass;
    }

}

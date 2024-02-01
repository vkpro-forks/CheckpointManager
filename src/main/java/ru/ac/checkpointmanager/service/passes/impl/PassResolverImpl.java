package ru.ac.checkpointmanager.service.passes.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.exception.CriticalServerException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.service.passes.PassResolver;

import java.util.UUID;

@Component
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
    @Transactional
    public Pass createPass(PassCreateDTO passCreateDTO) {
        UUID userId = passCreateDTO.getUserId();
        UUID territoryId = passCreateDTO.getTerritoryId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                });
        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(
                        () -> {
                            log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                            return new TerritoryNotFoundException(ExceptionUtils
                                    .TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                        });
        Pass pass;
        if (passCreateDTO.getCar() != null) {
            CarBrandDTO brand = passCreateDTO.getCar().getBrand();
            CarBrand carBrand = carBrandRepository.findByBrand(brand.getBrand()).orElseGet(() -> {
                log.info("CarBrand saved to DB");
                return carBrandRepository.save(new CarBrand(brand.getBrand()));
            });
            PassAuto passAuto = passMapper.toPassAuto(passCreateDTO);
            Car car = passAuto.getCar();
            car.setBrand(carBrand);
            if (car.getId() == null) {
                car.setId(UUID.randomUUID());
            }
            pass = passAuto;
            log.debug("Setting up passAuto for auto: {}", car.getId());
        } else {
            if (passCreateDTO.getVisitor() != null) {
                PassWalk passWalk = passMapper.toPassWalk(passCreateDTO);
                Visitor visitor = passWalk.getVisitor();
                if (visitor.getId() == null) {
                    visitor.setId(UUID.randomUUID());
                }
                pass = passWalk;
                log.debug("Setting up passWalk for visitor: {}", visitor.getId());
            } else {
                log.error(ExceptionUtils.PASS_RESOLVING_ERROR);
                throw new CriticalServerException(ExceptionUtils.PASS_RESOLVING_ERROR);
            }
        }
        pass.setUser(user);
        pass.setTerritory(territory);
        return pass;
    }

}

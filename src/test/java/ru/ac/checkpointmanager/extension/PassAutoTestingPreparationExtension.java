package ru.ac.checkpointmanager.extension;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.ac.checkpointmanager.extension.annotation.PassWithRequiredEntities;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;

/**
 * Сохраняет в базу нужные для пропуска данные, а именно User, CarBrand, Car, Territory перед запуском теста;
 * Удаляет за собой эти данные;
 * Переносит в тестовый класс сохраненные сущности в качестве параметров
 */
@Slf4j
public class PassAutoTestingPreparationExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

    UserRepository userRepository;

    TerritoryRepository territoryRepository;

    CarRepository carRepository;

    PassRepository passRepository;

    CarBrandRepository carBrandRepository;

    SavedPassWithRequiredEntitiesDTO savedPassWithRequiredEntitiesDTO = new SavedPassWithRequiredEntitiesDTO();

    @Override
    public void afterEach(ExtensionContext context) {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        initRepos(context);
        saveTerritoryUserCarBrand();
        saveCar();
        PassAuto pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(
                savedPassWithRequiredEntitiesDTO.getUser(),
                savedPassWithRequiredEntitiesDTO.getTerritory(),
                savedPassWithRequiredEntitiesDTO.getCar());
        savedPassWithRequiredEntitiesDTO.setPass(passRepository.saveAndFlush(pass));
    }

    @Override
    public boolean supportsParameter(ParameterContext param, ExtensionContext extension) throws ParameterResolutionException {
        return param.isAnnotated(PassWithRequiredEntities.class)
                && param.getParameter().getType().equals(SavedPassWithRequiredEntitiesDTO.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return savedPassWithRequiredEntitiesDTO;
    }

    //https://stackoverflow.com/questions/56904620/junit-5-inject-spring-components-to-extension-beforeallcallback-afterallcall
    private void initRepos(ExtensionContext context) {
        log.info("Set up repositories to save necessary entities");
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        userRepository = applicationContext.getBean(UserRepository.class);
        passRepository = applicationContext.getBean(PassRepository.class);
        carBrandRepository = applicationContext.getBean(CarBrandRepository.class);
        carRepository = applicationContext.getBean(CarRepository.class);
        territoryRepository = applicationContext.getBean(TerritoryRepository.class);
    }

    private void saveTerritoryAndUser() {
        log.info("Saving territory and user");
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedPassWithRequiredEntitiesDTO.setUser(userRepository.saveAndFlush(user));
        territory.setUsers(List.of(savedPassWithRequiredEntitiesDTO.getUser()));
        savedPassWithRequiredEntitiesDTO.setTerritory(territoryRepository.saveAndFlush(territory));
    }

    private void saveTerritoryUserCarBrand() {
        saveTerritoryAndUser();
        CarBrand carBrand = TestUtils.getCarBrand();
        log.info("Saving car brand");
        savedPassWithRequiredEntitiesDTO.setCarBrand(carBrandRepository.saveAndFlush(carBrand));
    }

    private void saveCar() {
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedPassWithRequiredEntitiesDTO.getCarBrand());
        car.setId(TestUtils.getCarDto().getId());
        log.info("Saving car");
        savedPassWithRequiredEntitiesDTO.setCar(carRepository.saveAndFlush(car));
    }
}

package ru.ac.checkpointmanager.ext;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class UserTerritoryCarPassInRepositoryExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

    private UserRepository userRepository;

    TerritoryRepository territoryRepository;

    CarRepository carRepository;

    PassRepository passRepository;

    CarBrandRepository carBrandRepository;

    VisitorRepository visitorRepository;

    Territory savedTerritory;

    User savedUser;

    CarBrand savedCarBrand;

    Car savedCar;

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        //visitorRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    //https://stackoverflow.com/questions/56904620/junit-5-inject-spring-components-to-extension-beforeallcallback-afterallcall
    ////https://stackoverflow.com/a/61608210 pass variables to test class
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        log.info("Set up repositories to save necessary entities");
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        userRepository = applicationContext.getBean(UserRepository.class);
        passRepository = applicationContext.getBean(PassRepository.class);
        carBrandRepository = applicationContext.getBean(CarBrandRepository.class);
        carRepository = applicationContext.getBean(CarRepository.class);
        territoryRepository = applicationContext.getBean(TerritoryRepository.class);
        saveTerritoryUserCarBrand();
        saveCar();
        passParametersToTestInstance(context);
    }

    private void saveTerritoryAndUser() {
        log.info("Saving territory and user");
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        savedTerritory = territoryRepository.saveAndFlush(territory);
    }

    private void saveTerritoryUserCarBrand() {
        saveTerritoryAndUser();
        CarBrand carBrand = TestUtils.getCarBrand();
        log.info("Saving car brand");
        savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
    }

    private void saveCar() {
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        log.info("Saving car");
        savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(ExtensionContext.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext; // сохранить сюда все сохраненные в бд сущности и передать как параметр в тест
    }

    private void passParametersToTestInstance(ExtensionContext context) throws NoSuchFieldException, IllegalAccessException {
        Object testInstance = context.getRequiredTestInstance();
        Field resultField = testInstance.getClass().getDeclaredField("car");
        resultField.setAccessible(true);
        resultField.set(testInstance, savedCar);
    }
}

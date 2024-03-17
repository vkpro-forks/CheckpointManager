package ru.ac.checkpointmanager.extension;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.ac.checkpointmanager.extension.annotation.InjectSavedEntitiesForPassTest;
import ru.ac.checkpointmanager.extension.pass.CarAndVisitorPassTestPreparationRepositoryInjector;
import ru.ac.checkpointmanager.extension.pass.CarVisitorUserTerritoryDto;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;

@Slf4j
public class PassTestingPreparationExtension extends CarAndVisitorPassTestPreparationRepositoryInjector implements BeforeEachCallback,
        AfterEachCallback, ParameterResolver {

    CarVisitorUserTerritoryDto carVisitorUserTerritoryDto = new CarVisitorUserTerritoryDto();

    @Override
    public void afterEach(ExtensionContext context) {
        passRepository.deleteAll();
        carBrandRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        super.initRepos(context);
        log.info("Saving car");
        carVisitorUserTerritoryDto.setCarBrand(carBrandRepository.saveAndFlush(TestUtils.getCarBrand()));
        log.info("Saving territory and user");
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        carVisitorUserTerritoryDto.setUser(savedUser);
        territory.setUsers(List.of(savedUser));
        carVisitorUserTerritoryDto.setTerritory(territoryRepository.saveAndFlush(territory));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(InjectSavedEntitiesForPassTest.class)
                && parameterContext.getParameter().getType().equals(CarVisitorUserTerritoryDto.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return carVisitorUserTerritoryDto;
    }
}

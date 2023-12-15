package ru.ac.checkpointmanager.it;

import jakarta.persistence.EntityManager;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.dto.CarDTO;
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
import ru.ac.checkpointmanager.testcontainers.PostgresContainersConfig;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class})
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class CarControllerIntegrationTest extends PostgresContainersConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CarBrandRepository carBrandRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    PassRepository passRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @MockBean
    RedisCacheManager redisCacheManager;

    @BeforeEach
    void init() {
        Cache mockCache = Mockito.mock(Cache.class);
        Mockito.when(redisCacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
    }

    @AfterEach
    void clear() {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        userRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldAddCar() {
        CarBrand savedCarBrand = saveCarBrandInRepo();

        CarDTO carDTO = new CarDTO();
        carDTO.setLicensePlate(TestUtils.LICENSE_PLATE);
        carDTO.setBrand(savedCarBrand);

        String carDtoString = TestUtils.jsonStringFromObject(carDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CAR_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        List<Car> allCars = carRepository.findAll();
        Assertions.assertThat(allCars).hasSize(1);
        Car savedCar = allCars.get(0);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedCar.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licensePlate")
                        .value(Matchers.equalToIgnoringCase(carDTO.getLicensePlate())));
    }

    @Test
    @SneakyThrows
    void shouldDeleteCarIfItPresentInPass() {
        CarBrand carBrand = saveCarBrandInRepo();
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);

        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(carBrand);
        car.setId(TestUtils.getCarDto().getId());
        Car savedCar = carRepository.saveAndFlush(car);//save car and repo change its id

        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours();
        passAuto.setCar(savedCar);
        passAuto.setUser(savedUser);
        passAuto.setTerritory(savedTerritory);

        passRepository.saveAndFlush(passAuto);

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.CAR_URL + "/" + savedCar.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Optional<Car> optionalCar = carRepository.findById(savedCar.getId());
        Assertions.assertThat(optionalCar).isEmpty();
    }

    @Test
    @SneakyThrows
    void shouldFindCarsByUserId() {
        CarBrand carBrand = saveCarBrandInRepo();
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);

        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(carBrand);
        car.setId(TestUtils.getCarDto().getId());
        Car savedCar = carRepository.saveAndFlush(car);//save car and repo change its id

        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours();
        passAuto.setCar(savedCar);
        passAuto.setUser(savedUser);
        passAuto.setTerritory(savedTerritory);

        passRepository.saveAndFlush(passAuto);

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.CAR_USER_URL + "/" + savedUser.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(savedCar.getId().toString()));
    }

    private CarBrand saveCarBrandInRepo() {
        CarBrand carBrand = TestUtils.getCarBrand();
        return carBrandRepository.saveAndFlush(carBrand);
    }

}

package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.testcontainers.PostgresContainersConfig;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class})
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class PassControllerIntegrationTest extends PostgresContainersConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    PassRepository passRepository;

    @Autowired
    CarBrandRepository carBrandRepository;


    @AfterEach
    void clear() {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldAddPassWithNewCarIfCarNotExists() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);

        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTO();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setBrand(savedCarBrand);//set saved car brand, if no car brand in DB, 404 will be thrown
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()));
        List<Car> allCars = carRepository.findAll();
        Assertions.assertThat(allCars).hasSize(1);
        Car car = allCars.get(0);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.car.id").value(car.getId().toString()));
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(1);
    }

    @Test
    @SneakyThrows
    void shouldAddPassWithExistingCarsId() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        Car savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTO();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setBrand(savedCarBrand);//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(savedCar.getId());
        passCreateDTO.getCar().setLicensePlate(savedCar.getLicensePlate());
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.car.id").value(savedCar.getId().toString()));

        List<Car> allCars = carRepository.findAll();
        Assertions.assertThat(allCars).hasSize(1);//check if no added cars

        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(1);//check if only one pass here
    }

    @Test
    @SneakyThrows
    void shouldAddPassWithExistingCarInRepoButCarInDtoWillBeWithoutId() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        Car savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTO();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setBrand(savedCarBrand);//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(null);//don't pass ID of car, pass only license plate
        passCreateDTO.getCar().setLicensePlate(savedCar.getLicensePlate());
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()));

        org.junit.jupiter.api.Assertions.assertAll(
                () -> {
                    List<Car> carsByUserId = carRepository.findCarsByUserId(savedUser.getId());
                    Assertions.assertThat(carsByUserId).hasSize(1);
                    Car carInPass = carsByUserId.get(0);
                    resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.car.id")
                            .value(carInPass.getId().toString()));
                },
                () -> {
                    List<Car> allCars = carRepository.findAll();
                    Assertions.assertThat(allCars).hasSize(2);//check if no added cars
                },
                () -> {
                    List<Pass> allPasses = passRepository.findAll();
                    Assertions.assertThat(allPasses).hasSize(1);//check if only one pass here
                }
        );
    }

}

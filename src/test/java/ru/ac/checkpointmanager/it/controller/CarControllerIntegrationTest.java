package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@EnablePostgresAndRedisTestContainers
class CarControllerIntegrationTest {

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

    Territory savedTerritory;

    User savedUser;

    CarBrand savedCarBrand;

    Car savedCar;

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
        saveCarBrandInRepo();
        CarDTO carDTO = new CarDTO();
        carDTO.setLicensePlate(TestUtils.LICENSE_PLATE);
        carDTO.setBrand(TestUtils.getCarBrandDTO());
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
        //save car brand, user, territory, car, bind it to pass, save pass, delete car, check
        CarBrand carBrand = saveCarBrandInRepo();
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Car car = TestUtils.getCar(carBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
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
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Car car = TestUtils.getCar(carBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(passAuto);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.searchCarByUserId(savedUser.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(savedCar.getId().toString()));
    }

    @Test
    @SneakyThrows
    void shouldUpdateCarWithSameCarBrand() {
        String newLicensePlate = "А666ВХ666";
        CarBrand carBrand = saveCarBrandInRepo();
        Car car = TestUtils.getCar(carBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        CarDTO carDto = TestUtils.getCarDto();
        carDto.setLicensePlate(newLicensePlate);
        carDto.setBrand(TestUtils.getCarBrandDTO());
        String carDtoString = TestUtils.jsonStringFromObject(carDto);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.CAR_URL + "/" + savedCar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedCar.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licensePlate").value(newLicensePlate));
    }

    @Test
    @SneakyThrows
    void shouldUpdateCarWithNewCarBrand() {
        String newLicensePlate = "А666ВХ666";
        CarBrand carBrand = saveCarBrandInRepo();
        CarBrand anotherCarBrand = new CarBrand();
        String evilCarBrand = "EvilCar";
        anotherCarBrand.setBrand(evilCarBrand);
        carBrandRepository.save(anotherCarBrand);
        Car car = TestUtils.getCar(carBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        CarDTO carDto = TestUtils.getCarDto();
        carDto.setLicensePlate(newLicensePlate);
        carDto.setBrand(new CarBrandDTO(evilCarBrand));
        String carDtoString = TestUtils.jsonStringFromObject(carDto);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.CAR_URL + "/" + savedCar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedCar.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licensePlate").value(newLicensePlate))
                .andExpect(MockMvcResultMatchers.jsonPath("$.brand.brand").value(evilCarBrand));
    }

    @Test
    @SneakyThrows
    void searchByUserId_CarIncludedInFourPasses_ReturnOneCar() {
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passesToSave = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passesToSave.add(createPassWithStatusAndTime(i));
        }
        passRepository.saveAllAndFlush(passesToSave);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.searchCarByUserId(savedUser.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(savedCar.getId().toString()));
    }

    private CarBrand saveCarBrandInRepo() {
        CarBrand carBrand = TestUtils.getCarBrand();
        return carBrandRepository.saveAndFlush(carBrand);
    }

    private PassAuto createPassWithStatusAndTime(int i) {
        LocalDateTime baseLdt = LocalDateTime.of(2024, 1, 1, 23, 0, 1);
        PassAuto pass = new PassAuto();
        pass.setId(UUID.randomUUID());
        pass.setTimeType(PassTimeType.ONETIME);
        pass.setStartTime(baseLdt.plusHours(i));
        pass.setEndTime(baseLdt.plusHours(5 + i));
        pass.setStatus(PassStatus.ACTIVE);
        pass.setTerritory(savedTerritory);
        pass.setUser(savedUser);
        pass.setCar(savedCar);
        return pass;
    }

    private void saveTerritoryUserCarBrand() {
        saveTerritoryAndUser();
        savedCarBrand = saveCarBrandInRepo();
    }

    private void saveTerritoryAndUser() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        savedTerritory = territoryRepository.saveAndFlush(territory);
    }

    private void saveCar() {
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
    }

}

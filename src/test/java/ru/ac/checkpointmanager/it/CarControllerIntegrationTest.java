package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
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
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
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
class CarControllerIntegrationTest extends PostgresContainersConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CarBrandRepository carBrandRepository;

    @Autowired
    CarRepository carRepository;

    @AfterEach
    void clear() {
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldAddCar() {
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);

        CarDTO carDTO = new CarDTO();//save without id
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

}

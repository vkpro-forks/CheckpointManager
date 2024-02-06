package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.exception.handler.ErrorMessage;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@Slf4j
@EnablePostgresAndRedisTestContainers
class CarBrandControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    CarBrandRepository carBrandRepository;

    @AfterEach
    void clear() {
        carBrandRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void saveCarBrand_AllOk_SaveAndReturnSaved() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.saveCarBrand(TestUtils.getCarBrandDTO()));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.brand").value(TestUtils.getCarBrand().getBrand()));

        Assertions.assertThat(carBrandRepository.findAll()).hasSize(1).flatExtracting(CarBrand::getBrand)
                .containsExactly(TestUtils.getCarBrand().getBrand());
    }

    @Test
    @SneakyThrows
    void saveCarBrand_IfCarBrandAlreadyExistsWhenCreateNew_ReturnConflictError() {
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedBrand = carBrandRepository.saveAndFlush(carBrand);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.saveCarBrand(new CarBrandDTO(savedBrand.getBrand())));

        resultActions.andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.CONFLICT.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(ErrorMessage.OBJECT_ALREADY_EXISTS))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.CAR_BRAND_EXISTS.formatted(carBrand.getBrand())));
    }

    @Test
    @SneakyThrows
    void updateCarBrand_IfCarBrandAlreadyExists_ReturnConflictError() {
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedBrand = carBrandRepository.saveAndFlush(carBrand);
        CarBrand anotherCarBrand = new CarBrand();
        anotherCarBrand.setBrand("BatMobile");
        CarBrand savedAnotherCarBrand = carBrandRepository.saveAndFlush(anotherCarBrand);
        log.info("Two brands saved in repo: {} and {}", savedBrand.getBrand(), savedAnotherCarBrand.getBrand());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updateCarBrand(savedBrand.getId(),
                new CarBrandDTO(savedAnotherCarBrand.getBrand())));

        resultActions.andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.CONFLICT.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(ErrorMessage.OBJECT_ALREADY_EXISTS))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.CAR_BRAND_EXISTS.formatted(anotherCarBrand.getBrand())));
    }
}

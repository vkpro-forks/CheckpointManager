package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.PostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.security.filter.JwtAuthenticationFilter;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class})
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@Slf4j
class CarBrandControllerIntegrationTest extends PostgresTestContainersConfiguration {

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
    void shouldSaveCarBrand() {
        String carBrandString = TestUtils.jsonStringFromObject(TestUtils.getCarBrand());
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST.name(), UrlConstants.CAR_BRANDS_URL);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CAR_BRANDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carBrandString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.brand").value(TestUtils.getCarBrand().getBrand()));

        List<CarBrand> allBrands = carBrandRepository.findAll();
        Assertions.assertThat(allBrands).hasSize(1);
        CarBrand carBrand = allBrands.get(0);
        Assertions.assertThat(carBrand.getBrand()).isEqualTo(TestUtils.getCarBrand().getBrand());
    }

    @Test
    @SneakyThrows
    void shouldReturnConflictErrorIfCarBrandAlreadyExistsWhenCreateNew() {
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedBrand = carBrandRepository.saveAndFlush(carBrand);
        String carBrandString = TestUtils.jsonStringFromObject(savedBrand);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST.name(), UrlConstants.CAR_BRANDS_URL);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CAR_BRANDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carBrandString))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.CONFLICT.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(Matchers.startsWith("Object")))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("CarBrand")));
    }

    @Test
    @SneakyThrows
    void shouldReturnConflictErrorIfCarBrandAlreadyExistsWhenUpdateForExistingBrand() {
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedBrand = carBrandRepository.saveAndFlush(carBrand);
        CarBrand anotherCarBrand = new CarBrand();
        anotherCarBrand.setBrand("BatMobile");
        CarBrand savedAnotherCarBrand = carBrandRepository.saveAndFlush(anotherCarBrand);
        log.info("Two brand saved in repo: {} and {}", savedBrand.getBrand(), savedAnotherCarBrand.getBrand());
        savedAnotherCarBrand.setBrand(savedBrand.getBrand());
        String newCarBrandString = TestUtils.jsonStringFromObject(savedAnotherCarBrand);
        String putUrl = (UrlConstants.CAR_BRANDS_URL + "/" + savedBrand.getId());
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT.name(), putUrl);
        mockMvc.perform(MockMvcRequestBuilders.put(putUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCarBrandString))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.CONFLICT.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(Matchers.startsWith("Object")))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("CarBrand")));
    }

}

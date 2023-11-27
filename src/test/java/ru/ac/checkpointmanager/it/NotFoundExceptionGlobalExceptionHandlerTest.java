package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.it.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.it.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.testcontainers.PostgresContainersConfig;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class})
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class NotFoundExceptionGlobalExceptionHandlerTest extends PostgresContainersConfig {

    private static final String TERRITORY = "Territory";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;

    @MockBean
    PassRepository passRepository;

    @Autowired
    CheckpointRepository checkpointRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @AfterEach
    void clear() {
        checkpointRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    //CAR BRAND NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundExceptionForGetCarBrand() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.CAR_BRANDS_URL + "/" + TestUtils.CAR_BRAND_ID_STR))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.NOT_FOUND.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TIMESTAMP).isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE).isNotEmpty());
    }

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundExceptionForUpdateCarBrand() {
        CarBrand carBrand = TestUtils.getCarBrand();
        String contentString = TestUtils.jsonStringFromObject(carBrand);
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.put(UrlConstants.CAR_BRANDS_URL + "/" + TestUtils.CAR_BRAND_ID_STR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentString));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundExceptionForDeleteCarBrand() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.CAR_BRANDS_URL + "/" + TestUtils.CAR_BRAND_ID_STR));
        checkNotFoundFields(resultActions);
    }

    //AVATAR NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleAvatarNotFoundExceptionForGetAvatar() {
        Mockito.when(userRepository.findAvatarIdByUserId(Mockito.any()))
                .thenReturn(UUID.randomUUID());
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.AVATAR_URL + "/" + TestUtils.USER_ID));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleAvatarNotFoundExceptionIfUserDoesntHaveAvatar() {
        Mockito.when(userRepository.findAvatarIdByUserId(Mockito.any())).thenReturn(null);
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.AVATAR_URL + "/" + TestUtils.USER_ID));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleAvatarNotFoundExceptionForGetAvatarPreview() {
        Mockito.when(userRepository.findAvatarIdByUserId(Mockito.any()))
                .thenReturn(UUID.randomUUID());
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.AVATAR_URL_PREVIEW + "/" + TestUtils.USER_ID));
        checkNotFoundFields(resultActions);
    }

    //CHECKPOINT NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForMarkCrossing() {
        Mockito.when(passRepository.findById(TestUtils.PASS_ID)).thenReturn(Optional.of(
                new PassAuto()
        ));
        String crossingDto = TestUtils.jsonStringFromObject(TestUtils.getCrossingDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_MARK_URL)
                        .content(crossingDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL).value(Matchers.startsWith("Checkpoint")));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForGetCheckPoint() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.CHECKPOINT_URL + "/" + TestUtils.CHECKPOINT_ID));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForUpdateCheckPoint() {
        String checkPointDto = TestUtils.jsonStringFromObject(TestUtils.getCheckPointDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.CHECKPOINT_URL)
                .content(checkPointDto)
                .contentType(MediaType.APPLICATION_JSON));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForDeleteCheckPoint() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete(UrlConstants.CHECKPOINT_URL + "/" + TestUtils.CHECKPOINT_ID));
        checkNotFoundFields(resultActions);
    }

    //CROSSING NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleCrossingNotFoundExceptionForGetCrossing() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.CROSSING_URL + "/" + TestUtils.CROSSING_ID));
        checkNotFoundFields(resultActions);
    }

    //CAR NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleCarNotFoundExceptionForDeleteCar() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.CAR_URL + "/" + TestUtils.CAR_ID));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCarNotFoundExceptionForUpdateCar() {
        String updateCarDto = TestUtils.jsonStringFromObject(TestUtils.getCarDto());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.CAR_URL + "/" + TestUtils.CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateCarDto));
        checkNotFoundFields(resultActions);
    }

    //PASS NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void handlePassNotFoundExceptionForGetCrossing() {
        Mockito.when(passRepository.findById(TestUtils.PASS_ID)).thenReturn(Optional.empty());
        String crossingDto = TestUtils.jsonStringFromObject(TestUtils.getCrossingDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_MARK_URL)
                        .content(crossingDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("Pass")));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionForGetPass() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_URL + "/" + TestUtils.PASS_ID));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionForUpdatePass() {
        String passUpdateDto = TestUtils.jsonStringFromObject(TestUtils.getPassDtoUpdate());
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passUpdateDto));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionForDeletePass() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.PASS_URL + "/" + TestUtils.PASS_ID));
        checkNotFoundFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("passUrlsForPatchMethodsArguments")
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionsForPatchPassMethods(String url) {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.patch(url.formatted(TestUtils.PASS_ID)));
        checkNotFoundFields(resultActions);
    }

//TERRITORY NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForAddPass() {
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(new User()));
        String passDtoCreate = TestUtils.jsonStringFromObject(TestUtils.getPassDtoCreate());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passDtoCreate))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForGetPassesByTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.PASS_URL_TERRITORY.formatted(TestUtils.TERR_ID)));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForGetTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.TERR_URL + "/" + TestUtils.TERR_ID));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForAddCheckPoint() {
        CheckpointDTO checkPointDTO = TestUtils.getCheckPointDTO();
        String checkPointDto = TestUtils.jsonStringFromObject(checkPointDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post(UrlConstants.CHECKPOINT_URL)
                        .content(checkPointDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForUpdateCheckPoint() {
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setName(TestUtils.CHECKPOINT_NAME);
        checkpoint.setType(CheckpointType.AUTO);
        Territory territory = new Territory();
        territory.setName("name");
        territoryRepository.save(territory);
        checkpoint.setTerritory(territory);
        Checkpoint savedCheckPoint = checkpointRepository.save(checkpoint);
        CheckpointDTO checkPointDTO = TestUtils.getCheckPointDTO();
        checkPointDTO.setId(savedCheckPoint.getId());
        String checkPointDto = TestUtils.jsonStringFromObject(checkPointDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put(UrlConstants.CHECKPOINT_URL)
                        .content(checkPointDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForGetUsersByTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.TERR_USERS_URL.formatted(TestUtils.TERR_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForUpdateTerritory() {
        String territoryDto = TestUtils.jsonStringFromObject(TestUtils.getTerritoryDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.TERR_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(territoryDto))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForAttachUserToTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .patch(UrlConstants.TERR_ATTACH_URL.formatted(TestUtils.TERR_ID, TestUtils.USER_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        checkNotFoundFields(resultActions);
    }


    private void checkNotFoundFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.NOT_FOUND.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TIMESTAMP).isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE).isNotEmpty());
    }

    private static Stream<String> passUrlsForPatchMethodsArguments() {
        return Stream.of(
                UrlConstants.PASS_URL_FAVORITE,
                UrlConstants.PASS_URL_UNWARNING,
                UrlConstants.PASS_URL_ACTIVATE,
                UrlConstants.PASS_URL_CANCEL,
                UrlConstants.PASS_URL_NOT_FAVORITE
        );
    }

}

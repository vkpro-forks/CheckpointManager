package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.Optional;
import java.util.stream.Stream;

class NotFoundExceptionGlobalExceptionHandlerTest extends GlobalExceptionHandlerBasicTestConfig {

    private static final String TERRITORY = "Territory";
    private static final String PHONE = "Phone";

    @Autowired
    UserRepository userRepository;

    @MockBean
    PassRepository passRepository;

    @Autowired
    CheckpointRepository checkpointRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    CarBrandRepository carBrandRepository;

    @Autowired
    CarRepository carRepository;

    @AfterEach
    void clear() {
        checkpointRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
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
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundExceptionForAddCar() {
        CarDTO carDTO = TestUtils.getCarDto();
        String carDtoString = TestUtils.jsonStringFromObject(carDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CAR_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("CarBrand")));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundExceptionForUpdateCar() {
        String newLicensePlate = "А666ВХ666";
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedBrand = carBrandRepository.saveAndFlush(carBrand);
        CarBrand anotherCarBrand = new CarBrand();
        String evilCarBrand = "EvilCar";
        anotherCarBrand.setBrand(evilCarBrand);
        Car car = TestUtils.getCar(savedBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        CarDTO carDto = TestUtils.getCarDto();
        carDto.setLicensePlate(newLicensePlate);
        carDto.setBrand(new CarBrandDTO(evilCarBrand));
        String carDtoString = TestUtils.jsonStringFromObject(carDto);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put(UrlConstants.CAR_URL + "/" + savedCar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("CarBrand")));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundExceptionForDeleteCarBrand() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.CAR_BRANDS_URL + "/" + TestUtils.CAR_BRAND_ID_STR));
        TestUtils.checkNotFoundFields(resultActions);
    }

    //AVATAR NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleAvatarNotFoundExceptionForGetAvatar() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.AVATAR_URL + "/" + TestUtils.USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL).value(Matchers.startsWith("Avatar")));
        TestUtils.checkNotFoundFields(resultActions);
    }

    //CHECKPOINT NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForMarkCrossing() {
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(PassStatus.ACTIVE);
        Mockito.when(passRepository.findById(TestUtils.PASS_ID)).thenReturn(Optional.of(passAuto));
        String crossingDto = TestUtils.jsonStringFromObject(TestUtils.getCrossingDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL)
                        .content(crossingDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL).value(Matchers.startsWith("Checkpoint")));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForGetCheckPoint() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.CHECKPOINT_URL + "/" + TestUtils.CHECKPOINT_ID));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForUpdateCheckPoint() {
        String checkPointDto = TestUtils.jsonStringFromObject(TestUtils.getCheckPointDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.CHECKPOINT_URL)
                .content(checkPointDto)
                .contentType(MediaType.APPLICATION_JSON));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCheckPointNotFoundExceptionForDeleteCheckPoint() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete(UrlConstants.CHECKPOINT_URL + "/" + TestUtils.CHECKPOINT_ID));
        TestUtils.checkNotFoundFields(resultActions);
    }

    //CROSSING NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleCrossingNotFoundExceptionForGetCrossing() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.CROSSING_URL + "/" + TestUtils.CROSSING_ID));
        TestUtils.checkNotFoundFields(resultActions);
    }

    //CAR NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleCarNotFoundExceptionForDeleteCar() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.CAR_URL + "/" + TestUtils.CAR_ID));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleCarNotFoundExceptionForUpdateCar() {
        String updateCarDto = TestUtils.jsonStringFromObject(TestUtils.getCarDto());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.CAR_URL + "/" + TestUtils.CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateCarDto));
        TestUtils.checkNotFoundFields(resultActions);
    }

    //PASS NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void handlePassNotFoundExceptionForMarkCrossing() {
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setName(TestUtils.CHECKPOINT_NAME);
        checkpoint.setType(CheckpointType.AUTO);
        Territory territory = new Territory();
        territory.setName("name");
        territoryRepository.save(territory);
        checkpoint.setTerritory(territory);
        Checkpoint savedCheckPoint = checkpointRepository.save(checkpoint);
        CrossingDTO crossingDTO = TestUtils.getCrossingDTO();
        crossingDTO.setCheckpointId(savedCheckPoint.getId());
        String crossingDto = TestUtils.jsonStringFromObject(crossingDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL)
                        .content(crossingDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("Pass")));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionForGetPass() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_URL + "/" + TestUtils.PASS_ID));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionForUpdatePass() {
        String passUpdateDto = TestUtils.jsonStringFromObject(TestUtils.getPassUpdateDTO());
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passUpdateDto));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionForDeletePass() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.PASS_URL + "/" + TestUtils.PASS_ID));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("passUrlsForPatchMethodsArguments")
    @SneakyThrows
    void shouldHandlePassNotFoundExceptionsForPatchPassMethods(String url) {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.patch(url.formatted(TestUtils.PASS_ID)));
        TestUtils.checkNotFoundFields(resultActions);
    }

//TERRITORY NOT FOUND EXCEPTION HANDLING

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForAddPass() {
        User savedUser = userRepository.save(TestUtils.getUser());
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        String passDtoCreate = TestUtils.jsonStringFromObject(passCreateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passDtoCreate))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForGetPassesByTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.PASS_URL_TERRITORY.formatted(TestUtils.TERR_ID)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForGetTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.TERR_URL + "/" + TestUtils.TERR_ID));
        TestUtils.checkNotFoundFields(resultActions);
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
        TestUtils.checkNotFoundFields(resultActions);
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
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForGetUsersByTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.TERR_USERS_URL.formatted(TestUtils.TERR_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        TestUtils.checkNotFoundFields(resultActions);
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
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForAttachUserToTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .patch(UrlConstants.TERR_ATTACH_DETACH_URL.formatted(TestUtils.TERR_ID, TestUtils.USER_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForDeleteTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete(UrlConstants.TERR_URL + "/" + TestUtils.TERR_ID))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleTerritoryNotFoundExceptionForDetachUserToTerritory() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete(UrlConstants.TERR_ATTACH_DETACH_URL.formatted(TestUtils.TERR_ID, TestUtils.USER_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(TERRITORY)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    //PHONE NOT FOUND EXCEPTION HANDLING
    @Test
    @SneakyThrows
    void shouldHandlePhoneNotFoundExceptionForGetNumber() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.PHONE_URL + "/" + TestUtils.PHONE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(PHONE)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePhoneNotFoundExceptionForDeleteNumber() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete(UrlConstants.PHONE_URL + "/" + TestUtils.PHONE_ID))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(PHONE)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandlePhoneNotFoundExceptionForUpdateNumber() {
        String phoneDto = TestUtils.jsonStringFromObject(TestUtils.getPhoneDto());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDto)
                )
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(PHONE)));
        TestUtils.checkNotFoundFields(resultActions);
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

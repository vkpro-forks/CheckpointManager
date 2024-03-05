package ru.ac.checkpointmanager.service.passes;

import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.assertion.AssertPass;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.exception.CriticalServerException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.exception.pass.ModifyPassException;
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.extension.argprovider.CarWithFieldsWithBrandArgumentProvider;
import ru.ac.checkpointmanager.extension.argprovider.PassForExceptionInPassResolverArgumentsProvider;
import ru.ac.checkpointmanager.extension.argprovider.VisitorWithFieldsArgumentProvider;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.service.passes.impl.PassResolverImpl;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PassResolverImplTest {

    @Mock
    TerritoryRepository territoryRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CarBrandRepository carBrandRepository;

    @InjectMocks
    PassResolverImpl passResolver;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(passResolver, "passMapper", new PassMapper(new ModelMapper()), PassMapper.class);
    }

    @Test
    void shouldReturnPassAutoWithSetUserAndTerritoryIfCarHasIdIfBrandExists() {
        User user = TestUtils.getUser();
        Territory territory = TestUtils.getTerritory();
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(territory));
        Mockito.when(carBrandRepository.findByBrand(Mockito.any())).thenReturn(Optional.of(TestUtils.getCarBrand()));
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();

        Pass pass = passResolver.createPass(passCreateDTO);

        Assertions.assertThat(pass).isInstanceOf(PassAuto.class);
        Assertions.assertThat(pass.getUser()).isEqualTo(user);
        Assertions.assertThat(pass.getTerritory()).isEqualTo(territory);
        PassAuto casted = (PassAuto) pass;
        Car car = casted.getCar();
        Assertions.assertThat(car.getId()).isEqualTo(TestUtils.CAR_ID);
        Assertions.assertThat(car.getBrand().getBrand()).isEqualTo(TestUtils.getCarBrand().getBrand());
        Mockito.verify(userRepository).findById(TestUtils.USER_ID);
        Mockito.verify(territoryRepository).findById(TestUtils.TERR_ID);
        Mockito.verify(carBrandRepository).findByBrand(car.getBrand().getBrand());
    }

    @Test
    void shouldReturnPassAutoWithSetUserAndTerritoryIfCarHasIdIfBrandNotExists() {
        User user = TestUtils.getUser();
        Territory territory = TestUtils.getTerritory();
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(territory));
        Mockito.when(carBrandRepository.findByBrand(Mockito.any())).thenReturn(Optional.empty());
        String notExistedCarBrand = "HateMobile";
        CarBrand createdCarBrand = new CarBrand(notExistedCarBrand);
        Mockito.when(carBrandRepository.save(Mockito.any())).thenReturn(createdCarBrand);
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        assert passCreateDTO.getCar() != null;
        passCreateDTO.getCar().setBrand(new CarBrandDTO(notExistedCarBrand));

        Pass pass = passResolver.createPass(passCreateDTO);

        Assertions.assertThat(pass).isInstanceOf(PassAuto.class);
        Assertions.assertThat(pass.getUser()).isEqualTo(user);
        Assertions.assertThat(pass.getTerritory()).isEqualTo(territory);
        PassAuto casted = (PassAuto) pass;
        Car car = casted.getCar();
        Assertions.assertThat(car.getId()).isEqualTo(TestUtils.CAR_ID);
        Assertions.assertThat(car.getBrand().getBrand()).isEqualTo(notExistedCarBrand);
        Mockito.verify(userRepository).findById(TestUtils.USER_ID);
        Mockito.verify(territoryRepository).findById(TestUtils.TERR_ID);
        Mockito.verify(carBrandRepository).findByBrand(car.getBrand().getBrand());
        Mockito.verify(carBrandRepository).save(Mockito.any());
    }

    @Test
    void shouldReturnPassWalkWithSetUserAndTerritory() {
        User user = TestUtils.getUser();
        Territory territory = TestUtils.getTerritory();
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(territory));
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithVisitor();

        Pass pass = passResolver.createPass(passCreateDTO);

        Assertions.assertThat(pass).isInstanceOf(PassWalk.class);
        Assertions.assertThat(pass.getUser()).isEqualTo(user);
        Assertions.assertThat(pass.getTerritory()).isEqualTo(territory);
        PassWalk casted = (PassWalk) pass;

        Assertions.assertThat(casted.getVisitor().getName()).isEqualTo(TestUtils.getVisitorDTO().getName());
        Mockito.verify(userRepository).findById(TestUtils.USER_ID);
        Mockito.verify(territoryRepository).findById(TestUtils.TERR_ID);
        Mockito.verifyNoInteractions(carBrandRepository);
    }

    @Test
    void shouldThrowExceptionIfUserNotExists() {
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.empty());
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithVisitor();

        Assertions.assertThatThrownBy(() -> passResolver.createPass(passCreateDTO))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionIfTerritoryNoExists() {
        User user = TestUtils.getUser();
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.empty());
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithVisitor();

        Assertions.assertThatThrownBy(() -> passResolver.createPass(passCreateDTO))
                .isInstanceOf(TerritoryNotFoundException.class);
    }

    @ParameterizedTest
    @ArgumentsSource(CarWithFieldsWithBrandArgumentProvider.class)
    void updatePass_UpdateWithCarCarBrandInRepo_ReturnUpdatedPass(CarDTO carDTO, Triple<String, String, String> fields) {
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setCar(carDTO);
        Car car = TestUtils.getCar(TestUtils.getCarBrand());
        Pass pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(), null, car);
        Mockito.when(carBrandRepository.findByBrand(Mockito.anyString())).thenReturn(Optional.of(TestUtils.getCarBrand()));

        Pass updatedPass = passResolver.updatePass(passUpdateDTO, pass);

        AssertPass.assertThat(updatedPass).isPassAutoWithMatchedCarFields(
                fields.getLeft() == null ? car.getLicensePlate() : carDTO.getLicensePlate(),
                fields.getMiddle() == null ? car.getPhone() : carDTO.getPhone(),
                TestUtils.getCarBrand());
        Mockito.verify(carBrandRepository).findByBrand(Mockito.anyString());
    }

    @Test
    void updateCar_UpdateCarWithDTOWithoutBrand_ReturnUpdatedPass() {
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        Car car = TestUtils.getCar(TestUtils.getCarBrand());
        Pass pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(), null, car);
        assert passUpdateDTO.getCar() != null;
        passUpdateDTO.getCar().setBrand(null);

        Pass updatedPass = passResolver.updatePass(passUpdateDTO, pass);

        CarDTO carDTO = passUpdateDTO.getCar();
        AssertPass.assertThat(updatedPass).isPassAutoWithMatchedCarFields(
                carDTO.getLicensePlate(),
                carDTO.getPhone(), TestUtils.getCarBrand());
        Mockito.verifyNoInteractions(carBrandRepository);
    }

    @Test
    void updatePass_UpdateWithCarCarBrandNotInRepo_ReturnUpdatedPass() {
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        Pass pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(), null,
                TestUtils.getCar(TestUtils.getCarBrand()));
        Mockito.when(carBrandRepository.findByBrand(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(carBrandRepository.save(Mockito.any())).thenReturn(TestUtils.getCarBrand());

        Pass updatedPass = passResolver.updatePass(passUpdateDTO, pass);

        AssertPass.assertThat(updatedPass).isPassAutoWithMatchedCarFields(
                TestUtils.getCarDto().getLicensePlate(), TestUtils.getCarDto().getPhone(), TestUtils.getCarBrand());
        Mockito.verify(carBrandRepository).findByBrand(Mockito.anyString());
        Mockito.verify(carBrandRepository).save(Mockito.any());
    }

    @ParameterizedTest
    @ArgumentsSource(VisitorWithFieldsArgumentProvider.class)
    void updatePass_UpdateWithVisitor_ReturnUpdatedPass(VisitorDTO visitorDTO, Triple<String, String, String> fields) {
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOVisitor();
        passUpdateDTO.setVisitor(visitorDTO);
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Pass pass = PassTestData.getSimpleActiveOneTimePassWalkFor3Hours(TestUtils.getUser(), null, visitor);

        Pass updatedPass = passResolver.updatePass(passUpdateDTO, pass);

        AssertPass.assertThat(updatedPass).isPassWalkWithMatchedVisitorFields(
                fields.getLeft() == null ? visitor.getName() : visitorDTO.getName(),
                fields.getMiddle() == null ? visitor.getPhone() : visitorDTO.getPhone(),
                fields.getRight() == null ? visitor.getNote() : visitorDTO.getNote());
    }

    @ParameterizedTest
    @ArgumentsSource(PassForExceptionInPassResolverArgumentsProvider.class)
    void updatePass_TryToChangePassType_ThrowException(Pass pass, PassUpdateDTO passUpdateDTO, String exceptionMsg) {
        Assertions.assertThatExceptionOfType(ModifyPassException.class).isThrownBy(
                        () -> passResolver.updatePass(passUpdateDTO, pass))
                .withMessage(exceptionMsg)
                .isInstanceOf(PassException.class);
    }

    @Test
    void updatePass_NoCarNoVisitor_ThrowException() {
        PassUpdateDTO passUpdateDTO = new PassUpdateDTO();
        PassAuto passAuto = new PassAuto();
        Assertions.assertThatExceptionOfType(CriticalServerException.class).isThrownBy(
                        () -> passResolver.updatePass(passUpdateDTO, passAuto))
                .withMessage(ExceptionUtils.PASS_RESOLVING_ERROR);
    }
}

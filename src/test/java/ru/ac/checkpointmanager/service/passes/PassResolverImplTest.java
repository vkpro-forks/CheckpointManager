package ru.ac.checkpointmanager.service.passes;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
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
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();

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
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        String notExistedCarBrand = "HateMobile";
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
    }

    @Test
    void shouldReturnPassWalkWithSetUserAndTerritory() {
        User user = TestUtils.getUser();
        Territory territory = TestUtils.getTerritory();
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(territory));
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();

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
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();

        Assertions.assertThatThrownBy(() -> passResolver.createPass(passCreateDTO))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionIfTerritoryNoExists() {
        User user = TestUtils.getUser();
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.empty());
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();

        Assertions.assertThatThrownBy(() -> passResolver.createPass(passCreateDTO))
                .isInstanceOf(TerritoryNotFoundException.class);
    }

}

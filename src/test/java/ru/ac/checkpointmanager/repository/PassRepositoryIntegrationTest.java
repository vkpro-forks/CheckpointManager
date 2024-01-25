package ru.ac.checkpointmanager.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassConstant;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.specification.PassSpecification;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext
@Testcontainers
@Slf4j
class PassRepositoryIntegrationTest {

    @Container
    @ServiceConnection(type = JdbcConnectionDetails.class)
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("chpmanDB");

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.label-filter", () -> "!demo-data");
    }

    @Autowired
    PassRepository passRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    VisitorRepository visitorRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    CarBrandRepository carBrandRepository;

    User savedUser;

    Territory savedTerritory;

    Visitor savedVisitor;

    CarBrand savedCarBrand;

    Car savedCar;

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        visitorRepository.deleteAll();
        passRepository.deleteAll();
    }

    @ParameterizedTest
    @EnumSource(PassStatus.class)
    void findPassesByStatusAndTimeBefore_StatusBeforeEndTime_ReturnListOfPasses(PassStatus status) {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        PassWalk passWalk = setUpPassWalk(status, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        PassWalk passWalk2 = setUpPassWalk(status, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(3),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        passRepository.saveAllAndFlush(List.of(passWalk, passWalk2));

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(status, PassConstant.END_TIME,
                LocalDateTime.now().plusDays(2));

        Assertions.assertThat(passes).hasSize(1).flatExtracting(Pass::getId).containsOnly(passWalk.getId());
    }

    @ParameterizedTest
    @EnumSource(PassStatus.class)
    void findPassesByStatusAndTimeBefore_StatusBeforeStart_ReturnListOfPasses(PassStatus status) {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        PassWalk passWalk = setUpPassWalk(status, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        PassWalk passWalk2 = setUpPassWalk(status, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);

        passRepository.saveAllAndFlush(List.of(passWalk, passWalk2));

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(status, PassConstant.START_TIME,
                LocalDateTime.now().plusDays(2));

        Assertions.assertThat(passes).hasSize(1).flatExtracting(Pass::getId).containsOnly(passWalk.getId());
    }

    @Test
    void findAllWithCarSpec_PassAutoAndPassWalkInDB_ReturnPageWithPass() {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        saveCar("U123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.ACTIVE, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> carSpec = PassSpecification.byCarNumberPart("U");
        Specification<Pass> spec = Specification.where(carSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).hasSize(1)
                .flatExtracting(Pass::getId).contains(savedPassAuto.getId());
    }

    @Test
    void findAllWithVisitorSpec_PassAutoAndPassWalkInDB_ReturnPageWithPasses() {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        saveCar("U123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.DELAYED, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> visitorSpec = PassSpecification.byVisitorPart("U");
        Specification<Pass> spec = Specification.where(visitorSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).hasSize(1)
                .flatExtracting(Pass::getId).contains(savedPassWalk.getId());
    }

    @Test
    void findAllWithCarSpec_PassAutoAndPassWalkInDB_ReturnEmptyPage() {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        saveCar("H123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.ACTIVE, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> carSpec = PassSpecification.byCarNumberPart("U");
        Specification<Pass> spec = Specification.where(carSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).isEmpty();
    }

    @Test
    void findAllWithVisitorSpec_PassAutoAndPassWalkInDB_ReturnEmpty() {
        saveUserTerritoryVisitor("NONAME");
        saveCar("U123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.DELAYED, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> visitorSpec = PassSpecification.byVisitorPart("U");
        Specification<Pass> spec = Specification.where(visitorSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).isEmpty();
    }

    @Test
    void findAllWithVisitorAndCarSpec_PassAutoAndPassWalkInDB_ReturnPageWithPasses() {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        saveCar("U123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.DELAYED, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> visitorSpec = PassSpecification.byVisitorPart("U");
        Specification<Pass> carSpec = PassSpecification.byCarNumberPart("U");
        Specification<Pass> spec = Specification.where(visitorSpec).or(carSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).hasSize(2)
                .flatExtracting(Pass::getId).contains(savedPassAuto.getId(), savedPassWalk.getId());
    }

    @Test
    void findAllWithVisitorAndCarSpec_PassAutoAndPassWalkInDB_ReturnPageWithOnlyCarPass() {
        saveUserTerritoryVisitor("Noname");
        saveCar("U123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.DELAYED, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> visitorSpec = PassSpecification.byVisitorPart("U");
        Specification<Pass> carSpec = PassSpecification.byCarNumberPart("U");
        Specification<Pass> spec = Specification.where(visitorSpec).or(carSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).hasSize(1)
                .flatExtracting(Pass::getId).contains(savedPassAuto.getId());
    }

    @Test
    void findAllWithVisitorAndCarSpec_PassAutoAndPassWalkInDB_ReturnPageWithOnlyVisitorPass() {
        saveUserTerritoryVisitor(TestUtils.FULL_NAME);
        saveCar("H123QA799");
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk passWalk = setUpPassWalk(PassStatus.DELAYED, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedUser, savedTerritory, savedVisitor, PassTimeType.ONETIME);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        Pageable pageable = PageRequest.of(0, 100);
        Specification<Pass> visitorSpec = PassSpecification.byVisitorPart("U");
        Specification<Pass> carSpec = PassSpecification.byCarNumberPart("U");
        Specification<Pass> spec = Specification.where(visitorSpec).or(carSpec);
        log.info("All saved, go to check");

        Page<Pass> foundPasses = passRepository.findAll(spec, pageable);

        log.info("Found page: {}", foundPasses.getContent());
        Assertions.assertThat(foundPasses.getContent()).hasSize(1)
                .flatExtracting(Pass::getId).contains(savedPassWalk.getId());
    }

    /*@Test
    void findPassesByStatusAndTimeBefore_ExperimentalEnumAndPgTypesDoesntMatch_ReturnListOfPasses() {
        saveUserTerritoryVisitor();

        PassStatus status = PassStatus.STATUS_NOT_IN_PG;
        PassWalk passWalk = new PassWalk();
        passWalk.setStatus(status);
        passWalk.setStartTime(LocalDateTime.now().minusDays(1));
        passWalk.setEndTime(LocalDateTime.now().plusDays(1));
        passWalk.setUser(savedUser);
        passWalk.setTerritory(savedTerritory);
        passWalk.setVisitor(savedVisitor);
        passWalk.setTimeType(PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(status, "endTime", LocalDateTime.now().plusDays(2));

        Assertions.assertThat(passes).isNotEmpty();
        Assertions.assertThat(passes.get(0).getStatus()).isEqualTo(PassStatus.ACTIVE);
    }
*/

    private void saveUserTerritoryVisitor(String name) {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        savedTerritory = territoryRepository.saveAndFlush(territory);
        Visitor visitor = new Visitor();
        visitor.setName(name);
        visitor.setPhone(TestUtils.PHONE_NUM);
        savedVisitor = visitorRepository.saveAndFlush(visitor);
    }

    private void saveCar(String licensePlate) {
        CarBrand carBrand = TestUtils.getCarBrand();
        savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = new Car();
        car.setLicensePlate(licensePlate);
        car.setId(TestUtils.CAR_ID);
        car.setBrand(savedCarBrand);
        savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
    }

    private PassWalk setUpPassWalk(PassStatus passStatus, LocalDateTime startTime, LocalDateTime endTime, User savedUser,
                                   Territory savedTerritory, Visitor savedVisitor, PassTimeType passTimeType
    ) {
        PassWalk passWalk = new PassWalk();
        passWalk.setStatus(passStatus);
        passWalk.setStartTime(startTime);
        passWalk.setEndTime(endTime);
        passWalk.setUser(savedUser);
        passWalk.setDtype("WALK");
        passWalk.setTerritory(savedTerritory);
        passWalk.setVisitor(savedVisitor);//name USERNAME
        passWalk.setTimeType(passTimeType);
        return passWalk;
    }

}

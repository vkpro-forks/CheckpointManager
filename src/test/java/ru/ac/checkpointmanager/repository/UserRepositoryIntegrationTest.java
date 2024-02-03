package ru.ac.checkpointmanager.repository;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext
@EnablePostgresAndRedisTestContainers
class UserRepositoryIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AvatarRepository avatarRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @AfterEach
    void clear() {
        avatarRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findUserWithAvatarIdById_ReturnUserWithAvatarWithIdOnly() {
        Avatar avatar = TestUtils.getAvatar();
        Avatar savedAvatar = avatarRepository.saveAndFlush(avatar);
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        user.setAvatar(savedAvatar);
        User savedUser = userRepository.saveAndFlush(user);

        Optional<User> optionalUser = userRepository.findUserWithAvatarIdById(savedUser.getId());

        Assertions.assertThat(optionalUser).isPresent();
        Avatar userAvatar = optionalUser.get().getAvatar();
        Assertions.assertThat(userAvatar).isNotNull();
        Assertions.assertThat(userAvatar.getId()).isEqualTo(savedAvatar.getId());
        Assertions.assertThat(userAvatar.getPreview()).isNull();
    }

    @Test
    void shouldReturnUsersByTerritoryId() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        Model<User> userModel = TestUtils.getInstancioUserModel();
        List<User> users = Instancio.ofList(userModel).size(10).create();
        List<User> savedUsers = userRepository.saveAllAndFlush(users);
        territory.setUsers(savedUsers);
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Pageable pageable = PageRequest.of(0, 6);

        Page<User> userPage = userRepository.findUsersByTerritoryId(savedTerritory.getId(), pageable);
        Assertions.assertThat(userPage.getContent()).hasSize(6);
        Assertions.assertThat(userPage.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(userPage.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(userPage.getNumberOfElements()).isEqualTo(6);
        Assertions.assertThat(userPage.getNumber()).isZero();
        Assertions.assertThat(userPage.getSize()).isEqualTo(6);
    }
}

package ru.ac.checkpointmanager.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Slf4j
class UserRepositoryIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AvatarRepository avatarRepository;

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

}

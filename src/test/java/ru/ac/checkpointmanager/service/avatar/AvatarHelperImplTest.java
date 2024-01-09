package ru.ac.checkpointmanager.service.avatar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AvatarHelperImplTest {

    @InjectMocks
    public AvatarServiceImpl avatarService;
    @Mock
    private TerritoryRepository territoryRepository;
    @Mock
    private AvatarHelper avatarHelper;
    @Mock
    private MultipartFile multipartFile;

    @Test
    void whenUpdateTerritoryAvatarAndTerritoryExistsThenAvatarIsUpdated() {
        UUID territoryId = UUID.randomUUID();
        Avatar avatar = TestUtils.createTestAvatar();
        Territory territory = TestUtils.getTerritory();
        territory.setAvatar(avatar);
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.of(territory));
        avatarHelper.updateTerritoryAvatar(territoryId, avatar);
        Assertions.assertEquals(avatar, territory.getAvatar());
        verify(territoryRepository).findById(territoryId);
        verify(avatarHelper).updateTerritoryAvatar(territoryId, avatar);
    }
}


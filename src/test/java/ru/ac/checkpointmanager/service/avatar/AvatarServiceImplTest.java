package ru.ac.checkpointmanager.service.avatar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.avatar.AvatarMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.ac.checkpointmanager.service.avatar.AvatarServiceImpl.AVATAR_NOT_FOUND_MSG;

@ExtendWith(MockitoExtension.class)
public class AvatarServiceImplTest {

    @InjectMocks
    private AvatarServiceImpl avatarService;
    @Mock
    private AvatarRepository avatarRepository;
    @Mock
    private AvatarHelper avatarHelper;
    @Mock
    private TerritoryRepository territoryRepository;
    @Mock
    private AvatarMapper avatarMapper;
    @Mock
    private MultipartFile avatarFile;
    @Mock
    private UserRepository userRepository;

    @Test
    void whenValidIdThenReturnAvatarImageDTO() {
        UUID id = TestUtils.AVATAR_ID;
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarImageDTO expectedDto = new AvatarImageDTO();
        when(avatarRepository.findByTerritoryId(id)).thenReturn(Optional.of(avatar));
        when(avatarHelper.createAvatarImageDTO(avatar)).thenReturn(expectedDto);
        AvatarImageDTO result = avatarService.getAvatarImageByAvatarId(id);
        Assertions.assertEquals(expectedDto, result);
    }

    @Test
    void whenInvalidIdThenThrowAvatarNotFoundExceptionInGetAvatarImage() {
        UUID id = TestUtils.AVATAR_ID;
        when(avatarRepository.findByTerritoryId(id)).thenReturn(Optional.empty());
        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class, () -> {
            avatarService.getAvatarImageByAvatarId(id);
        });
        Assertions.assertEquals(AVATAR_NOT_FOUND_MSG.formatted(id), thrown.getMessage());
        verify(avatarRepository).findByTerritoryId(id);
    }

    @Test
    void whenValidIdThenReturnAvatar() {
        UUID id = TestUtils.AVATAR_ID;
        Avatar expectedAvatar = TestUtils.createTestAvatar();
        when(avatarRepository.findById(id)).thenReturn(Optional.of(expectedAvatar));
        Avatar result = avatarService.findAvatarById(id);
        Assertions.assertEquals(expectedAvatar, result);
        verify(avatarRepository).findById(id);
    }

    @Test
    void whenInvalidIdThenThrowAvatarNotFoundExceptionInFindAvatarById() {
        UUID id = TestUtils.AVATAR_ID;
        when(avatarRepository.findById(id)).thenReturn(Optional.empty());
        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class, () -> {
            avatarService.findAvatarById(id);
        });
        Assertions.assertEquals(AVATAR_NOT_FOUND_MSG.formatted(id), thrown.getMessage());
        verify(avatarRepository).findById(id);
    }

    @Test
    void whenAvatarExistsThenDelete() {
        UUID id = TestUtils.AVATAR_ID;
        Avatar avatar = TestUtils.createTestAvatar();
        when(avatarRepository.findById(id)).thenReturn(Optional.of(avatar));
        avatarService.deleteAvatarIfExists(id);
        verify(avatarRepository).delete(avatar);
    }

    @Test
    void whenGetAvatarByUserIdAndAvatarExistsThenReturnAvatarImageDTO() {
        UUID userId = TestUtils.USER_ID;
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarImageDTO avatarImageDTO = new AvatarImageDTO();
        when(avatarRepository.findByUserId(userId)).thenReturn(Optional.of(avatar));
        when(avatarHelper.createAvatarImageDTO(avatar)).thenReturn(avatarImageDTO);
        AvatarImageDTO actualAvatarImageDTO = avatarService.getAvatarByUserId(userId);
        Assertions.assertEquals(avatarImageDTO, actualAvatarImageDTO);
        verify(avatarRepository).findByUserId(userId);
        verify(avatarHelper).createAvatarImageDTO(avatar);
    }

    @Test
    void whenGetAvatarByUserIdAndAvatarDoesNotExistThenThrowException() {
        UUID uuid = TestUtils.USER_ID;
        when(avatarRepository.findByUserId(uuid)).thenReturn(Optional.empty());
        Assertions.assertThrows(AvatarNotFoundException.class, () -> avatarService.getAvatarByUserId(uuid));
        verify(avatarRepository).findByUserId(uuid);
    }

    @Test
    void whenUploadAvatarByTerritoryAndTerritoryExistsThenReturnAvatarDTO() {
        UUID territoryId = UUID.randomUUID();
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarDTO expectedAvatarDTO = new AvatarDTO();

        when(territoryRepository.findById(territoryId)).thenReturn(Optional.of(new Territory()));
        when(avatarHelper.getOrCreateAvatarByTerritory(territoryId)).thenReturn(avatar);
        doNothing().when(avatarHelper).configureAvatar(any(Avatar.class), any(MultipartFile.class));
        doNothing().when(avatarHelper).processAndSetAvatarImage(any(Avatar.class), any(MultipartFile.class));
        when(avatarHelper.saveAvatar(any(Avatar.class))).thenReturn(avatar);
        doNothing().when(avatarHelper).updateTerritoryAvatar(any(UUID.class), any(Avatar.class));
        when(avatarMapper.toAvatarDTO(any(Avatar.class))).thenReturn(expectedAvatarDTO);
        AvatarDTO actualAvatarDTO = avatarService.uploadAvatarByTerritory(territoryId, avatarFile);
        Assertions.assertEquals(expectedAvatarDTO, actualAvatarDTO);

        verify(territoryRepository).findById(territoryId);
        verify(avatarHelper).getOrCreateAvatarByTerritory(territoryId);
        verify(avatarHelper).configureAvatar(avatar, avatarFile);
        verify(avatarHelper).processAndSetAvatarImage(avatar, avatarFile);
        verify(avatarHelper).saveAvatar(avatar);
        verify(avatarHelper).updateTerritoryAvatar(territoryId, avatar);
        verify(avatarMapper).toAvatarDTO(avatar);
    }

    @Test
    void whenUploadAvatarByTerritoryAndTerritoryDoesNotExistThenThrowException() {
        UUID territoryId = UUID.randomUUID();
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.empty());
        assertThrows(TerritoryNotFoundException.class, () -> avatarService.uploadAvatarByTerritory(territoryId, avatarFile));
        verify(territoryRepository).findById(territoryId);
    }

    @Test
    void whenUploadAvatarAndUserExistsThenReturnAvatarDTO() {
        UUID userId = TestUtils.USER_ID;
        Avatar avatar = TestUtils.createTestAvatar();
        AvatarDTO avatarDTO = new AvatarDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(TestUtils.getUser()));
        when(avatarHelper.getOrCreateAvatar(userId)).thenReturn(avatar);
        doNothing().when(avatarHelper).configureAvatar(any(Avatar.class), any(MultipartFile.class));
        doNothing().when(avatarHelper).processAndSetAvatarImage(any(Avatar.class), any(MultipartFile.class));
        when(avatarHelper.saveAvatar(any(Avatar.class))).thenReturn(avatar);
        doNothing().when(avatarHelper).updateUserAvatar(any(UUID.class), any(Avatar.class));
        when(avatarMapper.toAvatarDTO(any(Avatar.class))).thenReturn(avatarDTO);

        AvatarDTO actualAvatarDTO = avatarService.uploadAvatar(userId, avatarFile);

        Assertions.assertEquals(avatarDTO, actualAvatarDTO);
        verify(userRepository).findById(eq(userId));
        verify(avatarHelper).getOrCreateAvatar(eq(userId));
        verify(avatarHelper).configureAvatar(any(Avatar.class), any(MultipartFile.class));
        verify(avatarHelper).processAndSetAvatarImage(any(Avatar.class), any(MultipartFile.class));
        verify(avatarHelper).saveAvatar(any(Avatar.class));
        verify(avatarHelper).updateUserAvatar(eq(userId), any(Avatar.class));
        verify(avatarMapper).toAvatarDTO(any(Avatar.class));
    }

    @Test
    void whenUploadAvatarAndUserDoesNotExistThenThrowException() {
        UUID userId = TestUtils.USER_ID;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            avatarService.uploadAvatar(userId, avatarFile);
        });
        verify(userRepository).findById(userId);
        verify(avatarHelper, never()).getOrCreateAvatar(any(UUID.class));
    }


}

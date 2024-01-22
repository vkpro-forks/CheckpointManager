package ru.ac.checkpointmanager.service.avatar;

import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.avatar.AvatarMapperImpl;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceImplTest {

    @Mock
    private AvatarRepository avatarRepository;

    @Mock
    private AvatarHelper avatarHelper;

    @Mock
    private TerritoryRepository territoryRepository;

    @Mock
    private MultipartFile avatarFile;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AvatarServiceImpl avatarService;

    private UUID id;

    private Avatar avatar;

    private UUID userId;

    private UUID territoryId;


    @BeforeEach
    void init() {
        id = TestUtils.AVATAR_ID;
        avatar = TestUtils.getAvatar();
        userId = TestUtils.USER_ID;
        territoryId = UUID.randomUUID();
        ReflectionTestUtils.setField(avatarService, "avatarMapper", new AvatarMapperImpl(new ModelMapper()));
    }

    @Test
    void deleteAvatarByUserId_AllOk_DeleteAvatar() {
        User user = TestUtils.getUser();
        Avatar avatar = new Avatar();
        avatar.setId(TestUtils.AVATAR_ID);
        user.setAvatar(avatar);
        when(userRepository.findUserWithAvatarIdById(TestUtils.USER_ID)).thenReturn(Optional.of(user));

        avatarService.deleteAvatarByUserId(TestUtils.USER_ID);

        verify(avatarRepository, Mockito.times(1)).deleteById(TestUtils.AVATAR_ID);
        verify(userRepository, Mockito.times(1)).findUserWithAvatarIdById(TestUtils.USER_ID);
    }

    @Test
    void deleteAvatarByUserId_UserNotFound_ThrowException() {
        when(userRepository.findUserWithAvatarIdById(TestUtils.USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> avatarService.deleteAvatarByUserId(TestUtils.USER_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(avatarRepository, never()).deleteById(TestUtils.AVATAR_ID);
        verify(userRepository, Mockito.times(1)).findUserWithAvatarIdById(TestUtils.USER_ID);
    }

    @Test
    void deleteAvatarByUserId_UserHasNoAvatar_ThrowException() {
        User user = TestUtils.getUser();
        when(userRepository.findUserWithAvatarIdById(TestUtils.USER_ID)).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(AvatarNotFoundException.class)
                .isThrownBy(() -> avatarService.deleteAvatarByUserId(TestUtils.USER_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(avatarRepository, never()).deleteById(TestUtils.AVATAR_ID);
        verify(userRepository, Mockito.times(1)).findUserWithAvatarIdById(TestUtils.USER_ID);
    }

    @Test
    void whenValidIdThenReturnAvatarImageDTO() {
        AvatarImageDTO expectedDto = new AvatarImageDTO();
        when(avatarRepository.findById(id)).thenReturn(Optional.of(avatar));
        when(avatarHelper.createAvatarImageDTO(avatar)).thenReturn(expectedDto);

        AvatarImageDTO result = avatarService.getAvatarImageByAvatarId(id);

        assertEquals(expectedDto, result);
    }

    @Test
    void whenInvalidIdThenThrowAvatarNotFoundExceptionInGetAvatarImageById() {
        when(avatarRepository.findById(id)).thenReturn(Optional.empty());

        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class,
                () -> avatarService.getAvatarImageByAvatarId(id));

        assertEquals(ExceptionUtils.AVATAR_NOT_FOUND.formatted(id), thrown.getMessage());
        verify(avatarRepository).findById(id);
    }

    @Test
    void getAvatarImageByTerritoryId_AllOk_ReturnDto() {
        AvatarImageDTO expectedDto = new AvatarImageDTO();
        Territory territory = TestUtils.getTerritory();
        territory.setAvatar(avatar);
        when(territoryRepository.findTerritoryByIdWithAvatar(TestUtils.TERR_ID)).thenReturn(Optional.of(territory));
        when(avatarHelper.createAvatarImageDTO(avatar)).thenReturn(expectedDto);

        AvatarImageDTO result = avatarService.getAvatarImageByAvatarId(TestUtils.TERR_ID);

        assertEquals(expectedDto, result);
    }

    @Test
    void getAvatarImageByTerritoryId_TerritoryNotFound_ThrowException() {
        when(territoryRepository.findTerritoryByIdWithAvatar(TestUtils.TERR_ID)).thenReturn(Optional.empty());

        TerritoryNotFoundException thrown = assertThrows(TerritoryNotFoundException.class,
                () -> avatarService.getAvatarImageByTerritoryId(TestUtils.TERR_ID));

        assertEquals(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(TestUtils.TERR_ID), thrown.getMessage());
        verify(territoryRepository).findTerritoryByIdWithAvatar(TestUtils.TERR_ID);
    }

    @Test
    void getAvatarImageByTerritoryId_AvatarNotFound_ThrowException() {
        when(territoryRepository.findTerritoryByIdWithAvatar(TestUtils.TERR_ID))
                .thenReturn(Optional.of(TestUtils.getTerritory()));

        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class,
                () -> avatarService.getAvatarImageByTerritoryId(TestUtils.TERR_ID));

        assertEquals(ExceptionUtils.AVATAR_NOT_FOUND_FOR_TERRITORY.formatted(TestUtils.TERR_ID), thrown.getMessage());
        verify(territoryRepository).findTerritoryByIdWithAvatar(TestUtils.TERR_ID);
    }

    @Test
    void whenValidIdThenReturnAvatar() {
        Avatar expectedAvatar = TestUtils.getAvatar();
        when(avatarRepository.findById(id)).thenReturn(Optional.of(expectedAvatar));

        Avatar result = avatarService.findAvatarById(id);

        assertEquals(expectedAvatar, result);
        verify(avatarRepository).findById(id);
    }

    @Test
    void whenInvalidIdThenThrowAvatarNotFoundExceptionInFindAvatarById() {
        when(avatarRepository.findById(id)).thenReturn(Optional.empty());

        AvatarNotFoundException thrown = assertThrows(AvatarNotFoundException.class,
                () -> avatarService.findAvatarById(id));

        assertEquals(ExceptionUtils.AVATAR_NOT_FOUND.formatted(id), thrown.getMessage());
        verify(avatarRepository).findById(id);
    }

    @Test
    void whenAvatarExistsThenDelete() {
        when(avatarRepository.findById(id)).thenReturn(Optional.of(avatar));

        avatarService.deleteAvatarIfExists(id);

        verify(avatarRepository).delete(avatar);
    }

    @Test
    void whenGetAvatarByUserIdAndAvatarExistsThenReturnAvatarImageDTO() {
        AvatarImageDTO avatarImageDTO = new AvatarImageDTO();
        when(avatarRepository.findByUserId(userId)).thenReturn(Optional.of(avatar));
        when(avatarHelper.createAvatarImageDTO(avatar)).thenReturn(avatarImageDTO);

        AvatarImageDTO actualAvatarImageDTO = avatarService.getAvatarByUserId(userId);

        assertEquals(avatarImageDTO, actualAvatarImageDTO);
        verify(avatarRepository).findByUserId(userId);
        verify(avatarHelper).createAvatarImageDTO(avatar);
    }

    @Test
    void whenGetAvatarByUserIdAndAvatarDoesNotExistThenThrowException() {
        when(avatarRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(AvatarNotFoundException.class, () -> avatarService.getAvatarByUserId(userId));

        verify(avatarRepository).findByUserId(userId);
    }

    @Test
    void whenUploadAvatarByTerritoryAndTerritoryExistsThenReturnAvatarDTO() {
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.of(new Territory()));
        when(avatarHelper.getOrCreateAvatarByTerritory(territoryId)).thenReturn(avatar);
        doNothing().when(avatarHelper).configureAvatar(any(Avatar.class), any(MultipartFile.class));
        doNothing().when(avatarHelper).processAndSetAvatarImage(any(Avatar.class), any(MultipartFile.class));
        when(avatarHelper.saveAvatar(any(Avatar.class))).thenReturn(avatar);
        doNothing().when(avatarHelper).updateTerritoryAvatar(any(UUID.class), any(Avatar.class));

        AvatarDTO actualAvatarDTO = avatarService.uploadAvatarByTerritory(territoryId, avatarFile);

        assertEquals(avatar.getMediaType(), actualAvatarDTO.getMediaType());
        verify(territoryRepository).findById(territoryId);
        verify(avatarHelper).getOrCreateAvatarByTerritory(territoryId);
        verify(avatarHelper).configureAvatar(avatar, avatarFile);
        verify(avatarHelper).processAndSetAvatarImage(avatar, avatarFile);
        verify(avatarHelper).saveAvatar(avatar);
        verify(avatarHelper).updateTerritoryAvatar(territoryId, avatar);
    }

    @Test
    void whenUploadAvatarByTerritoryAndTerritoryDoesNotExistThenThrowException() {
        when(territoryRepository.findById(territoryId)).thenReturn(Optional.empty());

        assertThrows(TerritoryNotFoundException.class,
                () -> avatarService.uploadAvatarByTerritory(territoryId, avatarFile));

        verify(territoryRepository).findById(territoryId);
    }

    @Test
    void whenUploadAvatarAndUserExistsThenReturnAvatarDTO() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(TestUtils.getUser()));
        when(avatarHelper.getOrCreateAvatar(userId)).thenReturn(avatar);
        doNothing().when(avatarHelper).configureAvatar(any(Avatar.class), any(MultipartFile.class));
        doNothing().when(avatarHelper).processAndSetAvatarImage(any(Avatar.class), any(MultipartFile.class));
        when(avatarHelper.saveAvatar(any(Avatar.class))).thenReturn(avatar);
        doNothing().when(avatarHelper).updateUserAvatar(any(UUID.class), any(Avatar.class));

        AvatarDTO actualAvatarDTO = avatarService.uploadAvatar(userId, avatarFile);

        assertEquals(avatar.getMediaType(), actualAvatarDTO.getMediaType());
        verify(userRepository).findById(userId);
        verify(avatarHelper).getOrCreateAvatar(userId);
        verify(avatarHelper).configureAvatar(any(Avatar.class), any(MultipartFile.class));
        verify(avatarHelper).processAndSetAvatarImage(any(Avatar.class), any(MultipartFile.class));
        verify(avatarHelper).saveAvatar(any(Avatar.class));
        verify(avatarHelper).updateUserAvatar(any(), any(Avatar.class));
    }

    @Test
    void whenUploadAvatarAndUserDoesNotExistThenThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> avatarService.uploadAvatar(userId, avatarFile));

        verify(userRepository).findById(userId);
        verify(avatarHelper, never()).getOrCreateAvatar(any(UUID.class));
    }

}

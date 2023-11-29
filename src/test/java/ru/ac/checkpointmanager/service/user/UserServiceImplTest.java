package ru.ac.checkpointmanager.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ac.checkpointmanager.configuration.CustomAuthenticationToken;
import ru.ac.checkpointmanager.dto.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.service.phone.PhoneService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private TerritoryMapper territoryMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TemporaryUserService temporaryUserService;
    @Mock
    private EmailService emailService;
    @Mock
    private PhoneService phoneService;

    private UserService out;

    private UUID userId;
    private User user;
    private UserResponseDTO userResponseDTO;

    private Collection<UserResponseDTO> userResponseDTOS;
    private Collection<User> users;

    private ChangePasswordRequest passwordRequest;
    private ChangeEmailRequest emailRequest;
    private TemporaryUser tempUser;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        out = new UserServiceImpl(userMapper, territoryMapper, userRepository, phoneRepository, passwordEncoder,
                temporaryUserService, emailService, phoneService);

        setUserArea();
        setSecurityContext();
        setChangePasswordRequest();
        setChangeEmailRequest();
        setTemporaryUser();
    }

    void setUserArea() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setPassword("1");
        user.setEmail("test@example.com");
        user.setRole(Role.ADMIN);

        userResponseDTO = new UserResponseDTO();

        users = List.of(user);
        userResponseDTOS = List.of(userResponseDTO);
    }

    void setSecurityContext() {
        Authentication authentication = new CustomAuthenticationToken(user, null, userId, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    void setChangePasswordRequest() {
        passwordRequest = new ChangePasswordRequest();
        passwordRequest.setCurrentPassword("1");
        passwordRequest.setNewPassword("newPassword");
        passwordRequest.setConfirmationPassword("newPassword");
    }

    void setChangeEmailRequest() {
        emailRequest = new ChangeEmailRequest();
        emailRequest.setNewEmail("newEmail@example.com");
    }

    void setTemporaryUser() {
        tempUser = new TemporaryUser();
        tempUser.setPreviousEmail("test@example.com");
        tempUser.setEmail("newEmail@example.com");
    }

    @Test
    void findById_UserExists_ReturnsUserResponseDTO() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = out.findById(userId);

        assertNotNull(result);
        assertEquals(userResponseDTO, result);
    }

    @Test
    void findById_UserDoesNotExist_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.findById(userId),
                "Should throw UserNotFoundException when user does not exist");
    }

    @Test
    void findTerritoriesByUserId_ReturnsListOfTerritoriesDTO() {
        List<Territory> territories = List.of(new Territory());
        when(userRepository.findTerritoriesByUserId(userId)).thenReturn(territories);

        List<TerritoryDTO> territoryDTOs = List.of(new TerritoryDTO());
        when(territoryMapper.toTerritoriesDTO(territories)).thenReturn(territoryDTOs);

        List<TerritoryDTO> result = out.findTerritoriesByUserId(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(territoryDTOs.size(), territories.size());
    }

    @Test
    void findTerritoriesByUserId_ThrowsTerritoryNotFoundException() {
        when(userRepository.findTerritoriesByUserId(userId)).thenReturn(Collections.emptyList());

        assertThrows(TerritoryNotFoundException.class, () -> out.findTerritoriesByUserId(userId));
    }


    @Test
    void findByName_ReturnCollectionUserResponseDTO() {
        when(userRepository.findUserByFullNameContainingIgnoreCase(anyString())).thenReturn(users);
        when(userMapper.toUserResponseDTOs(users)).thenReturn((List<UserResponseDTO>) userResponseDTOS);

        Collection<UserResponseDTO> result = out.findByName("Test");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void findByName_ThrowsUserNotFoundException() {
        when(userRepository.findUserByFullNameContainingIgnoreCase(anyString())).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> out.findByName(anyString()));
    }

    @Test
    void updateUser_SuccessfulUpdate_ReturnsUpdatedUser() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setId(userId);
        userPutDTO.setFullName("New Name");
        userPutDTO.setMainNumber("123456789");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(new UserResponseDTO());

        UserResponseDTO result = out.updateUser(userPutDTO);

        assertNotNull(result);
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    void updateUser_UserNotFound_ThrowsUserNotFoundException() {
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.updateUser(userPutDTO));
    }

    @Test
    void changePassword_SuccessfulChange_UpdatesPassword() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        out.changePassword(passwordRequest);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
        assertEquals(user.getPassword(), passwordEncoder.encode(passwordRequest.getNewPassword()));
    }

    @Test
    void changePassword_IncorrectCurrentPassword_ThrowsException() {
        passwordRequest.setCurrentPassword("wrongPassword");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> out.changePassword(passwordRequest),
                "Should throw IllegalStateException for incorrect current password");
    }

    @Test
    void changePassword_PasswordAndConfirmationDoNotMatch_ThrowsException() {
        passwordRequest.setConfirmationPassword("differentNewPassword");

        assertThrows(IllegalStateException.class, () -> out.changePassword(passwordRequest),
                "Should throw IllegalStateException when new password and confirmation do not match");
    }

/*    @Test
    void changeEmail_SuccessfulChange_ReturnsRequest() {
        when(mapper.toTemporaryUser(user)).thenReturn(new TemporaryUser());

        ChangeEmailRequest result = out.changeEmail(emailRequest);

        assertEquals(emailRequest, result);
        verify(emailService).sendEmailConfirm(anyString(), anyString());
        verify(temporaryUserService).create(any(TemporaryUser.class));
    }*/


    @Test
    void changeEmail_EmailSendFailure_ThrowsMailSendException() {
        when(userMapper.toTemporaryUser(user)).thenReturn(new TemporaryUser());
        doThrow(new MailSendException("failed")).when(emailService).sendEmailConfirm(anyString(), anyString());

        assertThrows(MailSendException.class, () -> out.changeEmail(emailRequest),
                "Should throw MailSendException on email send failure");
    }

    @Test
    void confirmEmail_SuccessfulConfirmation_UpdatesUserEmail() {
        String token = "validToken";
        when(temporaryUserService.findByVerifiedToken(token)).thenReturn(tempUser);
        when(userRepository.findByEmail(tempUser.getPreviousEmail())).thenReturn(Optional.of(user));

        out.confirmEmail(token);

        assertEquals("newEmail@example.com", user.getEmail());
        verify(userRepository).save(user);
        verify(temporaryUserService).delete(tempUser);
    }

    @Test
    void confirmEmail_InvalidOrExpiredToken_NoActionTaken() {
        String token = "invalidToken";
        when(temporaryUserService.findByVerifiedToken(token)).thenReturn(null);

        out.confirmEmail(token);

        verify(userRepository, never()).save(any(User.class));
        verify(temporaryUserService, never()).delete(any(TemporaryUser.class));
    }

    @Test
    void confirmEmail_UserNotFound_ThrowsUserNotFoundException() {
        String token = "validToken";
        when(temporaryUserService.findByVerifiedToken(token)).thenReturn(tempUser);
        when(userRepository.findByEmail(tempUser.getPreviousEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.confirmEmail(token));
    }

    @Test
    void changeRole_SuccessfulChange_ChangesUserRole() {
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setRole(Role.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        out.changeRole(userId, Role.MANAGER);

        assertEquals(Role.MANAGER, existingUser.getRole());
        verify(userRepository).save(existingUser);
    }

    @Test
    void changeRole_NoPermissionToChangeToAdmin_ThrowsAccessDeniedException() {
        User existingUser = new User();
        existingUser.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        user.setRole(Role.USER);

        assertThrows(AccessDeniedException.class, () -> out.changeRole(userId, Role.ADMIN));
    }

    @Test
    void changeRole_UserAlreadyHasRole_ThrowsIllegalStateException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> out.changeRole(userId, Role.ADMIN));
    }

    @Test
    void changeRole_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.changeRole(userId, Role.ADMIN));
    }

    @Test
    void updateBlockStatus_SuccessfulUpdate_ReturnsUpdatedUser() {
        user.setIsBlocked(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = out.updateBlockStatus(userId, true);

        assertTrue(user.getIsBlocked());
        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void updateBlockStatus_UserAlreadyHasStatus_ThrowsIllegalStateException() {
        user.setIsBlocked(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> out.updateBlockStatus(userId, true));
    }

    @Test
    void updateBlockStatus_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.updateBlockStatus(userId, true));
    }

    @Test
    void blockById_SuccessfulBlock_UpdatesUser() {
        user.setIsBlocked(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        out.blockById(userId);

        verify(userRepository).blockById(userId);
    }

    @Test
    void blockById_UserAlreadyBlocked_ThrowsIllegalStateException() {
        user.setIsBlocked(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> out.blockById(userId));
    }

    @Test
    void blockById_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.blockById(userId));
    }

    @Test
    void unblockById_SuccessfulUnblock_UpdatesUser() {
        user.setIsBlocked(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        out.unblockById(userId);

        verify(userRepository).unblockById(userId);
    }

    @Test
    void unblockById_UserAlreadyUnblocked_ThrowsIllegalStateException() {
        user.setIsBlocked(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> out.unblockById(userId));
    }

    @Test
    void unblockById_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.unblockById(userId));
    }

    @Test
    void deleteUser_SuccessfulDeletion_DeletesUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        out.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.deleteUser(userId));
    }

    @Test
    void getAll_UsersExist_ReturnsUserList() {
        when(userRepository.findAll()).thenReturn((List<User>) users);
        when(userMapper.toUserResponseDTOs(users)).thenReturn((List<UserResponseDTO>) userResponseDTOS);

        Collection<UserResponseDTO> result = out.getAll();

        assertFalse(result.isEmpty());
        verify(userRepository).findAll();
        verify(userMapper).toUserResponseDTOs(users);
    }

    @Test
    void getAll_NoUsersInDB_ThrowsUserNotFoundException() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> out.getAll());
    }

    @Test
    void findUsersPhoneNumbers_UserExists_ReturnsPhoneNumbers() {
        List<String> phoneNumbers = List.of("1234567890");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(phoneRepository.getNumbersByUserId(userId)).thenReturn(phoneNumbers);

        Collection<String> result = out.findUsersPhoneNumbers(userId);

        assertFalse(result.isEmpty());
        assertEquals(phoneNumbers, result);
        verify(phoneRepository).getNumbersByUserId(userId);
    }

    @Test
    void findUsersPhoneNumbers_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> out.findUsersPhoneNumbers(userId));
    }

    @Test
    void assignAvatarToUser_ValidInputs_AssignsAvatar() {
        Avatar avatar = new Avatar();

        out.assignAvatarToUser(userId, avatar);

        verify(userRepository).setAvatarForUser(avatar, userId);
    }

    @Test
    void findByPassId_ValidId_ReturnsUser() {
        UUID passId = UUID.randomUUID();
        when(userRepository.findByPassId(passId)).thenReturn(user);

        User result = out.findByPassId(passId);

        assertEquals(user, result);
        verify(userRepository).findByPassId(passId);
    }
}
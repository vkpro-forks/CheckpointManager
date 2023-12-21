package ru.ac.checkpointmanager.service.user;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.user.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.user.ConfirmChangeEmail;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.EmailVerificationTokenException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.AuthenticationFacade;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.service.email.EmailService;
import ru.ac.checkpointmanager.service.phone.PhoneService;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    AuthenticationFacade authenticationFacade;
    @Mock
    private EmailService emailService;
    @Mock
    private PhoneService phoneService;
    @Mock
    private RedisCacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void shouldFindById() {
        User user = TestUtils.getUser();
        UUID userId = user.getId();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toUserResponseDTO(user)).thenReturn(TestUtils.getUserResponseDTO());

        UserResponseDTO result = userService.findById(userId);

        Assertions.assertThat(result).isNotNull();
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    void shouldThrowsUserNotFoundException() {
        UUID userId = TestUtils.USER_ID;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.findById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        Mockito.verify(userRepository).findById(userId);
    }

    @Test
    void shouldFindTerritoriesByUserId() {
        UUID userId = TestUtils.USER_ID;
        List<Territory> territories = List.of(TestUtils.getTerritory());
        List<TerritoryDTO> territoryDTOs = List.of(TestUtils.getTerritoryDTO());

        Mockito.when(userRepository.findTerritoriesByUserId(userId)).thenReturn(territories);
        Mockito.when(territoryMapper.toTerritoriesDTO(territories)).thenReturn(territoryDTOs);

        List<TerritoryDTO> result = userService.findTerritoriesByUserId(userId);

        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(territories.size()).isEqualTo(territoryDTOs.size());
        Mockito.verify(userRepository).findTerritoriesByUserId(userId);
        Mockito.verify(territoryMapper).toTerritoriesDTO(territories);
    }

    @Test
    void shouldFindTerritoriesByUserIdAndReturnEmptyList() {
        UUID userId = TestUtils.USER_ID;
        List<Territory> territories = Collections.emptyList();
        List<TerritoryDTO> territoryDTOS = Collections.emptyList();
        Mockito.when(userRepository.findTerritoriesByUserId(userId)).thenReturn(territories);
        Mockito.when(territoryMapper.toTerritoriesDTO(territories)).thenReturn(territoryDTOS);

        Assertions.assertThatNoException().isThrownBy(() -> userService.findTerritoriesByUserId(userId));
        Mockito.verify(userRepository).findTerritoriesByUserId(userId);
        Mockito.verify(territoryMapper).toTerritoriesDTO(territories);
    }

    @Test
    void shouldFindByName() {
        List<User> users = List.of(TestUtils.getUser());
        List<UserResponseDTO> userResponseDTOS = List.of(TestUtils.getUserResponseDTO());

        Mockito.when(userRepository.findUserByFullNameContainingIgnoreCase(anyString())).thenReturn(users);
        Mockito.when(userMapper.toUserResponseDTOs(users)).thenReturn(userResponseDTOS);

        Collection<UserResponseDTO> result = userService.findByName(anyString());
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(users).hasSize(1);
        Assertions.assertThat(users.size()).isEqualTo(userResponseDTOS.size());
        Mockito.verify(userRepository).findUserByFullNameContainingIgnoreCase(anyString());
        Mockito.verify(userMapper).toUserResponseDTOs(users);
    }

    @Test
    void shouldFindByNameAndReturnEmptyList() {
        List<User> users = Collections.emptyList();
        List<UserResponseDTO> userResponseDTOS = Collections.emptyList();

        Mockito.when(userRepository.findUserByFullNameContainingIgnoreCase(anyString())).thenReturn(users);
        Mockito.when(userMapper.toUserResponseDTOs(users)).thenReturn(userResponseDTOS);

        Assertions.assertThatNoException().isThrownBy(() -> userService.findByName(anyString()));
        Mockito.verify(userRepository).findUserByFullNameContainingIgnoreCase(anyString());
        Mockito.verify(userMapper).toUserResponseDTOs(users);
    }

    @Test
    void shouldUpdateUser() {
        User user = TestUtils.getUser();
        UUID userId = user.getId();
        UserResponseDTO userResponseDTO = TestUtils.getUserResponseDTO();
        userResponseDTO.setId(userId);
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        userPutDTO.setId(userId);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);
        when(phoneService.createPhoneNumber(any(PhoneDTO.class))).thenReturn(any(PhoneDTO.class));

        UserResponseDTO result = userService.updateUser(userPutDTO);

        Assertions.assertThat(result).isNotNull().isEqualTo(userResponseDTO);
        Mockito.verify(userRepository).save(user);
        Mockito.verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    void updateUserShouldThrowsUserNotFoundException() {
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        UUID userId = TestUtils.USER_ID;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(
                        () -> userService.updateUser(userPutDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        Mockito.verify(userRepository).findById(userId);
    }

    @Test
    @Disabled(value = "Broken, need to fix")
    void shouldChangePassword() {
        ChangePasswordRequest request = TestUtils.getChangePasswordRequest();
        String newPassword = request.getNewPassword();
        User user = TestUtils.getUser();
        user.setPassword(TestUtils.PASSWORD);

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(user);
        Mockito.when(passwordEncoder.matches(user.getPassword(), request.getCurrentPassword())).thenReturn(true);
        Mockito.when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);

        userService.changePassword(request);
        Mockito.verify(passwordEncoder).encode(newPassword);
        Mockito.verify(userRepository).save(user);
        Assertions.assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @Disabled("broken, need to fix")
    void shouldThrowExceptionIfPassedPasswordDoesntMatchCurrent() {
        ChangePasswordRequest request = TestUtils.getChangePasswordRequest();

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(TestUtils.getUser());
        Mockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> userService.changePassword(request))
                .withMessageContaining("Current password not matched");
        Mockito.verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    @Disabled("broken, need to fix")
    void changePasswordWhenConfirmationDoNotMatchThrowsException() {
        ChangePasswordRequest request = TestUtils.getChangePasswordRequest();
        request.setConfirmationPassword("1");

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(TestUtils.getUser());
        Mockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> userService.changePassword(request))
                .withMessageContaining("Passwords are not the same");
    }

    @Test
    @Disabled("broken, need to fix")
    void successfulChangeEmailRequest() {
        ChangeEmailRequest request = TestUtils.getChangeEmailRequest();
        ConfirmChangeEmail confirm = TestUtils.getConfirmChangeEmail();
        User user = TestUtils.getUser();

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(user);
        Mockito.when(userRepository.findByEmail(request.getNewEmail())).thenReturn(Optional.empty());
        Mockito.when(userMapper.toConfirmChangeEmail(request)).thenReturn(confirm);
//        Mockito.when(cacheManager.getCache("email")).thenReturn(cache); //TODO проверить кэш в интеграционном тесте

        userService.changeEmail(request);
        Mockito.verify(userRepository).findByEmail(anyString());
        Mockito.verify(emailService).sendEmailConfirm(anyString(), anyString());
//        Mockito.verify(cache).put(confirmChangeEmail.getVerifiedToken(), confirmChangeEmail);
    }

    @Test
    @Disabled("broken, need to fix")
    void changeEmailShouldThrowExceptionWhenEmailTaken() {
        ChangeEmailRequest request = TestUtils.getChangeEmailRequest();
        User user = TestUtils.getUser();

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(user);
        Mockito.when(userRepository.findByEmail(request.getNewEmail())).thenReturn(Optional.ofNullable(user));

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> userService.changeEmail(request))
                .withMessageContaining("already taken");

        Mockito.verify(userRepository).findByEmail(anyString());
    }

    @Test
    @Disabled("broken, need to fix")
    void changeEmailThrowsMailSendException() {
        User user = TestUtils.getUser();
        ChangeEmailRequest request = TestUtils.getChangeEmailRequest();

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(user);
        Mockito.when(userMapper.toConfirmChangeEmail(request)).thenReturn(TestUtils.getConfirmChangeEmail());
        Mockito.doThrow(new MailSendException("failed")).when(emailService).sendEmailConfirm(anyString(), anyString());

        Assertions.assertThatExceptionOfType(MailSendException.class)
                .isThrownBy(() -> userService.changeEmail(request))
                .withMessageContaining("Email sending failed");
    }

    @Test
    void confirmEmailSuccessfulConfirmationUpdatesUserEmail() {
        User user = TestUtils.getUser();
        ConfirmChangeEmail changeEmail = TestUtils.getConfirmChangeEmail();
        String token = TestUtils.EMAIL_STRING_TOKEN;

        Mockito.when(cacheManager.getCache("email")).thenReturn(cache);
        Mockito.when(cacheManager.getCache("user")).thenReturn(cache);
        Mockito.when(cache.get(Mockito.eq(token), Mockito.eq(ConfirmChangeEmail.class))).thenReturn(changeEmail);
        Mockito.when(userRepository.findByEmail(changeEmail.getPreviousEmail())).thenReturn(Optional.of(user));
        Mockito.when(jwtService.generateAccessToken(user)).thenReturn("mockAccessToken");
        Mockito.when(jwtService.generateRefreshToken(user)).thenReturn("mockRefreshToken");

        userService.confirmEmail(token);

        Assertions.assertThat(user.getEmail()).isEqualTo(changeEmail.getNewEmail());
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void confirmEmailInvalidOrExpiredTokenNoActionTaken() {
        String token = "invalidToken";

        Assertions.assertThatThrownBy(
                        () -> userService.confirmEmail(token))
                .isInstanceOf(EmailVerificationTokenException.class)
                .hasMessageContaining("Invalid or expired token");

        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void confirmEmailUserNotFoundThrowsUserNotFoundException() {
        String token = "validToken";
        ConfirmChangeEmail changeEmail = TestUtils.getConfirmChangeEmail();

        Mockito.when(cacheManager.getCache("email")).thenReturn(cache);
        Mockito.when(cache.get(Mockito.eq(token), Mockito.eq(ConfirmChangeEmail.class))).thenReturn(changeEmail);
        when(userRepository.findByEmail(changeEmail.getPreviousEmail())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.confirmEmail(token))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with [email %s] not found", changeEmail.getPreviousEmail());

    }

    @Test
    @Disabled("broken, need to fix")
    void changeRoleSuccessfulChange() {
        User userInContext = TestUtils.getUser();
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        UUID userId = user.getId();
        TestUtils.setSecurityContext(userInContext);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId, Role.MANAGER);

        Assertions.assertThat(Role.MANAGER).isEqualTo(user.getRole());
        Mockito.verify(userRepository).save(user);
    }

    @Test
    @Disabled("broken, need to fix")
    void changeRoleNoPermissionToChangeToAdminThrowsAccessDeniedException() {
        User userInContext = TestUtils.getUser();
        userInContext.setRole(Role.MANAGER);
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        UUID userId = user.getId();

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(user);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Assertions.assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> userService.changeRole(userId, Role.ADMIN))
                .withMessageContaining("You do not have permission to change the role to ADMIN");
    }

    @Test
    @Disabled("broken, need to fix")
    void changeRoleNoPermissionToChangeAdminToAnotherThrowsAccessDeniedException() {
        User userInContext = TestUtils.getUser();
        userInContext.setRole(Role.MANAGER);
        User user = TestUtils.getUser();
        user.setRole(Role.ADMIN);
        UUID userId = user.getId();

        Mockito.when(authenticationFacade.getCurrentUser()).thenReturn(userInContext);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Assertions.assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> userService.changeRole(userId, Role.USER))
                .withMessageContaining("You do not have permission to change the role ADMIN to another role");
    }


    @Test
    @Disabled("broken, need to fix")
    void changeRoleUserAlreadyHasRoleThrowsIllegalStateException() {
        User userInContext = TestUtils.getUser();
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        UUID userId = user.getId();
        TestUtils.setSecurityContext(userInContext);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> userService.changeRole(userId, Role.USER))
                .withMessageContaining("This user already has role %s", user.getRole());
    }

    @Test
    void changeRoleUserNotFoundThrowsUserNotFoundException() {
        UUID userId = TestUtils.USER_ID;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.changeRole(userId, Role.ADMIN))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with [id=%s] not found", userId);
    }

    @Test
    void updateBlockStatusSuccessfulUpdate() {
        User user = TestUtils.getUser();
        UUID userId = TestUtils.USER_ID;
        user.setIsBlocked(false);
        UserResponseDTO userResponseDTO = TestUtils.getUserResponseDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.updateBlockStatus(userId, true);

        Assertions.assertThat(user.getIsBlocked()).isTrue();
        Assertions.assertThat(result).isNotNull();
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void updateBlockStatusUserNotFoundThrowsUserNotFoundException() {
        UUID userId = TestUtils.USER_ID;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.updateBlockStatus(userId, true))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with [id=%s] not found", userId);
    }

    @Test
    void blockByIdSuccessful() {
        User user = TestUtils.getUser();
        UUID userId = TestUtils.USER_ID;
        user.setIsBlocked(false);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.blockById(userId);

        Mockito.verify(userRepository).blockById(userId);
    }

    @Test
    void blockByIdUserAlreadyBlockedThrowsIllegalStateException() {
        User user = TestUtils.getUser();
        UUID userId = TestUtils.USER_ID;
        user.setIsBlocked(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> userService.blockById(userId))
                .withMessageContaining("User already blocked [id=%s]", userId);
    }

    @Test
    void blockByIdUserNotFoundThrowsUserNotFoundException() {
        UUID userId = TestUtils.USER_ID;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.blockById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with [id=%s] not found", userId);
    }

    @Test
    void unblockByIdSuccessfulUnblock() {
        User user = TestUtils.getUser();
        UUID userId = TestUtils.USER_ID;
        user.setIsBlocked(true);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.unblockById(userId);
        Mockito.verify(userRepository).unblockById(userId);
    }

    @Test
    void unblockByIdUserAlreadyUnblockedThrowsIllegalStateException() {
        User user = TestUtils.getUser();
        UUID userId = TestUtils.USER_ID;
        user.setIsBlocked(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> userService.unblockById(userId))
                .withMessageContaining("User already unblocked [id=%s]", userId);
    }

    @Test
    void unblockByIdUserNotFoundThrowsUserNotFoundException() {
        UUID userId = TestUtils.USER_ID;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.unblockById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with [id=%s] not found", userId);
    }

    @Test
    void deleteUserSuccessfulDeletion() {
        User user = TestUtils.getUser();
        UUID userId = TestUtils.USER_ID;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);
        Mockito.verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUserUserNotFoundThrowsUserNotFoundException() {
        UUID userId = TestUtils.USER_ID;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> userService.deleteUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with [id=%s] not found", userId);
    }

    @Test
    void getAllUsersExistReturnsUserList() {
        List<User> users = List.of(TestUtils.getUser());
        UserResponseDTO userResponseDTO = TestUtils.getUserResponseDTO();
        List<UserResponseDTO> userResponseDTOS = List.of(userResponseDTO);

        Mockito.when(userRepository.findAll()).thenReturn(users);
        Mockito.when(userMapper.toUserResponseDTOs(users)).thenReturn(userResponseDTOS);

        Collection<UserResponseDTO> result = userService.getAll();

        Assertions.assertThat(result).isNotEmpty().contains(userResponseDTO);
        Mockito.verify(userRepository).findAll();
        Mockito.verify(userMapper).toUserResponseDTOs(users);
    }

    @Test
    void findUsersPhoneNumbersUserExistsReturnsPhoneNumbers() {
        UUID userId = TestUtils.USER_ID;
        String phone = "1234567890";
        List<String> phoneNumbers = List.of(phone);
        when(phoneRepository.getNumbersByUserId(userId)).thenReturn(phoneNumbers);

        Collection<String> result = userService.findUsersPhoneNumbers(userId);

        Assertions.assertThat(result).isNotEmpty().contains(phone);
        Assertions.assertThat(phoneNumbers.size()).isEqualTo(result.size());
        Mockito.verify(phoneRepository).getNumbersByUserId(userId);
    }

    @Test
    void assignAvatarToUserValidInputsAssignsAvatar() {
        User user = TestUtils.getUser();
        Avatar avatar = user.getAvatar();
        UUID userId = user.getId();

        userService.assignAvatarToUser(userId, avatar);
        Mockito.verify(userRepository).setAvatarForUser(avatar, userId);
    }

    @Test
    void findByPassIdReturnsUser() {
        User user = TestUtils.getUser();
        UUID passId = TestUtils.PASS_ID;

        when(userRepository.findByPassId(passId)).thenReturn(user);
        User result = userService.findByPassId(passId);

        Assertions.assertThat(user).isEqualTo(result);
        Mockito.verify(userRepository).findByPassId(passId);
    }
}
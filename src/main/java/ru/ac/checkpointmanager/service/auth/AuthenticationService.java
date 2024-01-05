package ru.ac.checkpointmanager.service.auth;

import ru.ac.checkpointmanager.dto.user.AuthRequestDTO;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.PreAuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.ConfirmationRegistrationDTO;
import ru.ac.checkpointmanager.dto.user.LoginResponseDTO;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;

public interface AuthenticationService {

    ConfirmationRegistrationDTO preRegister(RegistrationDTO registrationDTO);

    void confirmRegistration(String token);

    PreAuthResponseDTO isUserAuthenticated(String email);

    LoginResponseDTO authenticate(AuthRequestDTO request);

    AuthResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO);
}
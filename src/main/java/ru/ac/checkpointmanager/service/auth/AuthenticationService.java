package ru.ac.checkpointmanager.service.auth;

import ru.ac.checkpointmanager.dto.user.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.user.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.user.IsAuthenticatedResponse;
import ru.ac.checkpointmanager.dto.user.ConfirmRegistration;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;

public interface AuthenticationService {

    ConfirmRegistration preRegister(UserAuthDTO userAuthDTO);

    void confirmRegistration(String token);

    IsAuthenticatedResponse isUserAuthenticated(String email);

    LoginResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(RefreshTokenDTO refreshTokenDTO);
}
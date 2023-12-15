package ru.ac.checkpointmanager.service.auth;

import ru.ac.checkpointmanager.dto.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.IsAuthenticatedResponse;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.model.TemporaryUser;

public interface AuthenticationService {

    TemporaryUser preRegister(UserAuthDTO userAuthDTO);

    void confirmRegistration(String token);

    IsAuthenticatedResponse isUserAuthenticated(String email);

    LoginResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(RefreshTokenDTO refreshTokenDTO);
}
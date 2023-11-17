package ru.ac.checkpointmanager.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.ac.checkpointmanager.dto.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.IsAuthenticatedResponse;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.model.User;

import java.io.IOException;

public interface AuthenticationService {

    TemporaryUser preRegister(UserAuthDTO userAuthDTO);

    void confirmRegistration(String token);

    IsAuthenticatedResponse isUserAuthenticated(String email);

    LoginResponse authenticate(AuthenticationRequest request);

    void saveUserToken(User user, String jwtToken);

    void revokeAllUserTokens(User user);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
package ru.ac.checkpointmanager.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.ac.checkpointmanager.dto.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.UserAuthDTO;

import java.io.IOException;

public interface AuthenticationService {
    UserAuthDTO createUser(UserAuthDTO userAuthDTO);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}

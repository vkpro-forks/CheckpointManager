package ru.ac.checkpointmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.ac.checkpointmanager.configuration.JwtAuthenticationFilter;
import ru.ac.checkpointmanager.configuration.JwtService;
import ru.ac.checkpointmanager.repository.TokenRepository;

@TestConfiguration
public class ValidationTestConfiguration {

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    JwtService jwtService;

    @MockBean
    TokenRepository tokenRepository;

    @MockBean
    UserDetailsService userDetailsService;

}

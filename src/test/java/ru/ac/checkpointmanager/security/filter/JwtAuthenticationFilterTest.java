package ru.ac.checkpointmanager.security.filter;

import jakarta.servlet.FilterChain;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtService jwtService;
    @Mock
    JwtValidator jwtValidator;

    @Mock
    UserDetailsService userDetailsService;

    @InjectMocks
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @SneakyThrows
    void shouldPassRequestWithoutHeaderToAuthentication() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/authentication");
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);
        Mockito.verify(mockFilterChain).doFilter(request, response);
    }



}
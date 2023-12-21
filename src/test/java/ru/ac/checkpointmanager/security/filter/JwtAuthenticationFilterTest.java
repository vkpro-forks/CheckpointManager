package ru.ac.checkpointmanager.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.util.TestUtils;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtService jwtService;
    @Mock
    JwtValidator jwtValidator;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    HandlerExceptionResolver resolver;

    @InjectMocks
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Captor
    ArgumentCaptor<RuntimeException> exceptionCaptor;

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

    @Test
    @SneakyThrows
    void shouldSendExceptionToResolverIfNoJwtInHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);
        Mockito.verifyNoInteractions(mockFilterChain);
        Mockito.verify(resolver).resolveException(Mockito.any(), Mockito.any(), Mockito.any(),
                exceptionCaptor.capture());
        RuntimeException captured = exceptionCaptor.getValue();
        Assertions.assertThat(captured).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @SneakyThrows
    void shouldSendExceptionToResolverIfJwtBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);
        Mockito.verifyNoInteractions(mockFilterChain);
        Mockito.verify(resolver).resolveException(Mockito.any(), Mockito.any(), Mockito.any(),
                exceptionCaptor.capture());
        RuntimeException captured = exceptionCaptor.getValue();
        Assertions.assertThat(captured).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @SneakyThrows
    void shouldSendExceptionToResolverIfJwtIsNotValid() {
        Mockito.when(jwtValidator.validateAccessToken(Mockito.any())).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + "stubJwt");
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);
        Mockito.verifyNoInteractions(mockFilterChain);
        Mockito.verify(resolver).resolveException(Mockito.any(), Mockito.any(), Mockito.any(),
                exceptionCaptor.capture());
        RuntimeException captured = exceptionCaptor.getValue();
        Assertions.assertThat(captured).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @SneakyThrows
    void shouldSendExceptionToResolverIfJwtIsExpired() {
        Mockito.when(jwtValidator.validateAccessToken(Mockito.any())).thenThrow(ExpiredJwtException.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + "stubJwt");
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);
        Mockito.verifyNoInteractions(mockFilterChain);
        Mockito.verify(resolver).resolveException(Mockito.any(), Mockito.any(), Mockito.any(),
                exceptionCaptor.capture());
        RuntimeException captured = exceptionCaptor.getValue();
        Assertions.assertThat(captured).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @SneakyThrows
    void shouldSendExceptionToResolverIfNoEmailInJwt() {
        String stubJwt = "stubJwt";
        Mockito.when(jwtValidator.validateAccessToken(Mockito.any())).thenReturn(true);
        Mockito.when(jwtService.extractUsername(stubJwt)).thenReturn(null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + stubJwt);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);
        Mockito.verifyNoInteractions(mockFilterChain);
        Mockito.verify(resolver).resolveException(Mockito.any(), Mockito.any(), Mockito.any(),
                exceptionCaptor.capture());
        RuntimeException captured = exceptionCaptor.getValue();
        Assertions.assertThat(captured).isInstanceOf(InvalidTokenException.class);
    }


}
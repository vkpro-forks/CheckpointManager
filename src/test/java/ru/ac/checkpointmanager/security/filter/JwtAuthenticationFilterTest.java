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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    public static final String STUB_JWT = "stubJwt";
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
    void shouldPassRequestToFilterIfNoJwtInHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);

        Mockito.verify(mockFilterChain).doFilter(request, response);
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
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + STUB_JWT);
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
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + STUB_JWT);
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
    void shouldSendExceptionToResolverIfUserFromJwtNotFound() {
        Mockito.when(jwtValidator.validateAccessToken(Mockito.any())).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + STUB_JWT);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(TestUtils.EMAIL);
        Mockito.when(userDetailsService.loadUserByUsername(TestUtils.EMAIL))
                .thenThrow(UsernameNotFoundException.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);

        Mockito.verifyNoInteractions(mockFilterChain);
        Mockito.verify(resolver).resolveException(Mockito.any(), Mockito.any(), Mockito.any(),
                exceptionCaptor.capture());
        RuntimeException captured = exceptionCaptor.getValue();
        Assertions.assertThat(captured).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @SneakyThrows
    void shouldPassRequestIfAuthenticationAlreadySet() {
        Mockito.when(jwtValidator.validateAccessToken(Mockito.any())).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + STUB_JWT);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContext mockContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockContext.getAuthentication()).thenReturn(Mockito.mock(Authentication.class));
        SecurityContextHolder.setContext(mockContext);

        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);

        Mockito.verify(mockFilterChain).doFilter(request, response);
    }

    @Test
    @SneakyThrows
    void shouldSetAuthenticationIfAllOk() {
        Mockito.when(jwtValidator.validateAccessToken(Mockito.any())).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TestUtils.AUTH_HEADER, TestUtils.BEARER + STUB_JWT);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(jwtService.extractUsername(Mockito.any())).thenReturn(TestUtils.EMAIL);
        UserDetails mockUserDetails = Mockito.mock(User.class);
        Mockito.when(userDetailsService.loadUserByUsername(TestUtils.EMAIL)).thenReturn(mockUserDetails);
        Mockito.when(jwtService.extractRole(Mockito.anyString())).thenReturn(List.of("ROLE_ADMIN"));
        SecurityContext mockContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(mockContext);

        jwtAuthenticationFilter.doFilterInternal(request, response, mockFilterChain);

        Mockito.verify(mockContext).setAuthentication(Mockito.any(CustomAuthenticationToken.class));
        Mockito.verify(mockFilterChain).doFilter(request, response);
    }

}

package ru.ac.checkpointmanager.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.security.jwt.JwtValidator;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Фильтр для аутентификации JWT.
 * <p>
 * Этот фильтр проверяет JWT токены, приходящие в заголовках запросов, и устанавливает контекст
 * безопасности Spring Security, если токен валиден. Он проверяет наличие токена, его валидность,
 * и на основе данных токена создает объект аутентификации в контексте безопасности.
 * <p>
 *
 * @author fifimova
 * @see JwtService
 * @see UserDetailsService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtValidator jwtValidator;
    private final UserDetailsService userDetailsService;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.debug("Method {} was invoked", MethodLog.getMethodName());

        if (request.getServletPath().contains("/api/v1/authentication")) {
            log.debug("Authentication path '{}' requested, passing through the filter chain.", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> jwtOpt = getJwtFromRequest(request);
        if (jwtOpt.isEmpty()) {
            log.debug("Authorization header doesn't contain bearer token");
            handlerExceptionResolver
                    .resolveException(request, response, null,
                            new AccessDeniedException("Jwt is not present in header, access forbidden for this path"));
            return;
        }
        String jwt = jwtOpt.get();
        try {
            if (StringUtils.isNotBlank(jwt) && jwtValidator.validateAccessToken(jwt)) {
                String userEmail = jwtService.extractUsername(jwt);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    if (jwtValidator.isUsernameValid(jwt, userDetails)) {
                        setAuthenticationContext(request, jwt, userDetails);
                    }
                } else {
                    log.debug("User email doesn't exists or ");
                }
            } else {
                log.debug("Invalid JWT token [{}]", jwt);
                handlerExceptionResolver
                        .resolveException(request, response, null, new InvalidTokenException("Jwt is invalid"));
                return;
            }
        } catch (ExpiredJwtException exception) {
            log.warn("Jwt is expired");
            handlerExceptionResolver.resolveException(request, response, null, exception);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }

    private void setAuthenticationContext(HttpServletRequest request, String jwt, UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = jwtService.extractRole(jwt).stream()
                .map(SimpleGrantedAuthority::new).toList();

        UUID userId = null;
        if (userDetails instanceof User) {
            userId = ((User) userDetails).getId();
        }

        CustomAuthenticationToken authToken = new CustomAuthenticationToken(
                userDetails, null, userId, authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("Authentication set for user '{}'", userDetails.getUsername());
    }
}
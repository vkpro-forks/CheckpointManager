package ru.ac.checkpointmanager.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.TokenRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

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
 * @see TokenRepository
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    /**
     * Фильтрует входящие HTTP запросы для аутентификации пользователя по JWT токену.
     * <p>
     * Метод анализирует HTTP запросы на наличие JWT в заголовке 'Authorization'.
     * В случае наличия валидного токена происходит установка аутентификационных данных
     * в контексте безопасности Spring Security. Запросы без токена или с невалидным токеном
     * пропускаются без изменения контекста безопасности.
     * <p>
     *
     * @param request     Объект запроса {@link HttpServletRequest}.
     * @param response    Объект ответа {@link HttpServletResponse}.
     * @param filterChain Цепочка фильтров {@link FilterChain}.
     * @throws ServletException В случае ошибок фильтрации.
     * @throws IOException      В случае ошибок ввода-вывода.
     * @see CustomAuthenticationToken
     * @see JwtService
     * @see SecurityContextHolder
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.debug("Method {} was invoked", MethodLog.getMethodName());
        if (request.getServletPath().contains("/api/v1/authentication")) { // содержит ли путь сервлета в запросе /authentication
            log.debug("Authentication path '{}' requested, passing through the filter chain.", request.getRequestURI());
            filterChain.doFilter(request, response); // если содержит, вызывается doFilter для разрешения продолжения обработки запроса следующему фильтру или сервлету в цепочке
            return;
        }
        final String authHeader = request.getHeader("Authorization"); // получается значение заголовка "Authorization" из запроса и сохраняется в переменной
        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found in the request header, passing through the filter chain.");
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7); //извлекаем заголовок с 7 символа, то что идет после "Bearer "
        userEmail = jwtService.extractUsername(jwt);  // извлекаем username из токена (sub)
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) { // если email есть в токене и в SecurityContextHolder нет информации об аутентификации
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            boolean isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())//  isExpired() - не истек лисрок действия токена.  isRevoked() - не был ли токен отозван
                    .orElse(false);
            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) { // если токен валидный создаем объект для секурити контекст холдера
                log.info("JWT is valid for user '{}', setting the security context.", userEmail);
                Collection<? extends GrantedAuthority> authorities = jwtService.extractRole(jwt).stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                UUID userId = null;
                if (userDetails instanceof User) {
                    userId = ((User) userDetails).getId();
                }

                CustomAuthenticationToken authToken = new CustomAuthenticationToken(
                        userDetails, null, userId, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
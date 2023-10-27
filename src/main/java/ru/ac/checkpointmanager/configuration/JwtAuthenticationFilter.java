package ru.ac.checkpointmanager.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.ac.checkpointmanager.repository.TokenRepository;

import java.io.IOException;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/chpman/authentication")) { // содержит ли путь сервлета в запросе /authentication
            filterChain.doFilter(request, response); // если содержит, вызывается doFilter для разрешения продолжения обработки запроса следующему фильтру или сервлету в цепочке
            return;
        }
        final String authHeader = request.getHeader("Authorization"); // получается значение заголовка "Authorization" из запроса и сохраняется в переменной
        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( // представляет аутентификацию пользователя
                        userDetails,
                        null, // пароль не передается, так как аутентификация осуществляется с помощью токена
                        jwtService.extractRole(jwt).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request) // собирает инфу о запросе
                );
                SecurityContextHolder.getContext().setAuthentication(authToken); // устанавливаем пользователя в контекст ходер
            }
        }
        filterChain.doFilter(request, response);
    }
}

package ru.ac.checkpointmanager.security.jwt.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.security.jwt.JwtService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис для работы с JSON Web Token (JWT).
 * <p>
 * Этот класс обеспечивает централизованное управление JWT, включая их создание, анализ и проверку.
 * JWT используются в приложении для аутентификации и авторизации, позволяя безопасно передавать
 * информацию о пользователе между клиентом и сервером.
 * <p>
 * Ключевые возможности сервиса включают:<br>
 * - Генерация токенов доступа и токенов обновления;<br>
 * - Извлечение информации (как username, roles, userId) из токенов;<br>
 * - Проверка валидности токена, включая проверку срока его действия и соответствия пользователя.<br>
 * <p>
 * Класс работает с секретными ключами для подписи токенов, обеспечивая их безопасность и подлинность.
 *
 * @author fifimova
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see io.jsonwebtoken.Claims
 * @see io.jsonwebtoken.Jwts
 * @see io.jsonwebtoken.security.Keys
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Извлекает имя пользователя из токена.
     * <p>
     * Этот метод возвращает имя пользователя, закодированное в JWT токене.
     * Имя пользователя хранится в поле 'subject' токена.
     * <p>
     *
     * @param token JWT токен, из которого необходимо извлечь имя пользователя.
     * @return Имя пользователя, закодированное в токене.
     */
    @Override
    public String extractUsername(String token) {
        log.info("Method {} [Token {}]", MethodLog.getMethodName(), token);
        String username = extractClaim(token, Claims::getSubject);
        if (username == null || username.isBlank()) {
            throw new InvalidTokenException("Username/email in JWT is null or empty");
        }
        return username;
    }

    /**
     * Извлекает роль пользователя из токена.
     * <p>
     * Этот метод возвращает список ролей пользователя, закодированный в JWT токене.
     * Роли хранятся в специальном поле токена.
     * <p>
     *
     * @param token JWT токен, из которого необходимо извлечь роли пользователя.
     * @return Список ролей пользователя, закодированных в токене.
     */
    @Override
    public List<String> extractRole(String token) {
        log.info("Method {} [Token {}]", MethodLog.getMethodName(), token);
        List<?> roles = extractAllClaims(token).get("role", List.class);
        if (roles == null) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Извлекает идентификатор пользователя из токена.
     * <p>
     * Этот метод возвращает уникальный идентификатор пользователя (UUID),
     * закодированный в JWT токене.
     * <p>
     *
     * @param token JWT токен, из которого необходимо извлечь идентификатор пользователя.
     * @return UUID пользователя, закодированный в токене.
     */
    @Override
    public UUID extractId(String token) {
        log.info("Method {} [Token {}]", MethodLog.getMethodName(), token);
        String id = extractAllClaims(token).get("id", String.class);
        if (id == null) {
            throw new InvalidTokenException("Jwt hasn't ID claim");
        }
        return UUID.fromString(id);
    }

    /**
     * Извлекает специфическое утверждение (claim) из JWT токена.
     * <p>
     * Этот метод использует функцию-резолвер для извлечения и возврата конкретного утверждения из токена.
     * Резолвер определяет тип возвращаемого значения.
     * <p>
     *
     * @param <T>            Тип возвращаемого значения утверждения.
     * @param token          Строка JWT токена.
     * @param claimsResolver Функция, преобразующая {@link Claims} в тип T.
     * @return Утверждение из токена, преобразованное в тип T.
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Генерирует JWT токен для указанных пользовательских данных.
     * <p>
     * Этот метод предназначен для создания токена JWT в процессе аутентификации пользователя.
     * Он включает стандартный набор утверждений (claims), таких, как имя пользователя и роли.
     * В случае, если пользовательские данные являются экземпляром класса {@link User},
     * дополнительно добавляется идентификатор пользователя.
     * <p>
     * Метод возвращает сгенерированный токен, подписанный с использованием алгоритма HS256.
     *
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @return Строка со сгенерированным JWT токеном.
     */
    @Override
    public String generateAccessToken(UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        Map<String, Object> extraClaims = new HashMap<>();
        List<String> rolesList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        extraClaims.put("role", rolesList);

        if (userDetails instanceof User user) {
            extraClaims.put("id", user.getId());
        }

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Генерирует токен обновления для указанных пользовательских данных.
     * <p>
     * Этот метод предназначен для создания токена обновления, который выдается пользователям
     * вместе с основным токеном доступа. Токен обновления используется для получения нового токена доступа,
     * когда срок действия текущего токена доступа истекает. Это обеспечивает непрерывный доступ
     * пользователя к системе без необходимости повторной аутентификации.
     * Токен содержит уникальный идентификатор пользователя (если userDetails является экземпляром {@link User})
     * и дополнительный claim, определяющий его как токен обновления.
     * <p>
     * Токен подписывается с использованием алгоритма HS256 и имеет установленное время жизни.
     *
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @return Строка со сгенерированным токеном обновления.
     */
    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        Map<String, Object> extraClaims = new HashMap<>();
        User user = (User) userDetails;

        extraClaims.put("id", user.getId());
        extraClaims.put("refresh", true);

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает все утверждения (claims) из JWT токена.
     * <p>
     * Этот метод извлекает и возвращает объект {@link Claims}, содержащий все утверждения,
     * закодированные в токене. Для парсинга токена используется ключ подписи.
     * <p>
     *
     * @param token Строка JWT токена.
     * @return Объект {@link Claims}, содержащий все утверждения токена.
     */
    @Override
    public Claims extractAllClaims(String token) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Генерирует ключ для подписи JWT токенов.
     * <p>
     * Этот метод декодирует секретный ключ из формата Base64 и создает объект {@link Key},
     * который используется для подписи JWT токенов с помощью алгоритма HMAC-SHA.
     * HMAC (Hash-based Message Authentication Code) представляет собой тип кода аутентификации сообщений,
     * который сочетает криптографическую хеш-функцию с секретным ключом, обеспечивая тем самым безопасность
     * и подлинность данных.
     * <p>
     * Метод использует секретный ключ, который должен быть предварительно определен и хранится в безопасном месте.
     * <p>
     *
     * @return Объект {@link Key}, используемый для подписи JWT токенов.
     */
    private Key getSignInKey() {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

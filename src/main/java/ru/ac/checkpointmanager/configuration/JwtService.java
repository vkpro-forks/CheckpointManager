package ru.ac.checkpointmanager.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}") // 1 день
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")  // 7 дней
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
    public List<String> extractRole(String token) {
        log.info("Method {} [Token {}]", MethodLog.getMethodName(), token);
        return extractAllClaims(token).get("role", List.class);
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
    public UUID extractId(String token) {
        log.info("Method {} [Token {}]", MethodLog.getMethodName(), token);
        return extractAllClaims(token).get("id", UUID.class);
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
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Генерирует JWT токен для указанных пользовательских данных.
     * <p>
     * Метод предназначен для создания токена при аутентификации и регистрации пользователя.
     * Использует стандартный набор утверждений.
     * <p>
     *
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @return Строка с сгенерированным JWT токеном.
     */
    public String generateToken(UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Генерирует JWT токен с дополнительными утверждениями для указанных пользовательских данных.
     * <p>
     * Этот метод позволяет добавить дополнительные утверждения в токен.
     * <p>
     *
     * @param extraClaims Карта с дополнительными утверждениями.
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @return Строка с сгенерированным JWT токеном.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Генерирует токен обновления для указанных пользовательских данных.
     * <p>
     * Метод предназначен для создания токена обновления, который используется при регистрации
     * и аутентификации пользователя для последующего обновления основного токена доступа.
     * <p>
     *
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @return Строка с сгенерированным токеном обновления.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Создаёт JWT токен для аутентификации пользователя.
     * <p>
     * Этот метод генерирует JWT токен на основе предоставленных утверждений (claims),
     * деталей пользователя и заданного времени истечения. В токен включаются роли пользователя
     * и его идентификатор (UUID), если он доступен. Токен подписывается с использованием
     * заданного ключа подписи и алгоритма HS256.
     * <p>
     *
     * @param extraClaims Дополнительные утверждения, которые необходимо включить в токен.
     * @param userDetails Объект, содержащий информацию о пользователе, для которого создаётся токен.
     * @param expiration  Время истечения срока действия токена в миллисекундах.
     * @return Строковое представление JWT токена.
     * @see Jwts
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        List<String> rolesList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        extraClaims.put("role", rolesList); // добавляем в клеймы токена роль юзера

        if (userDetails instanceof User user) {
            extraClaims.put("id", user.getId());
        }

        return Jwts
                .builder()
                .setClaims(extraClaims) // устанавливает доп клеймы, которые мы хотим вшить в токен
                .setSubject(userDetails.getUsername()) // sub == username
                .setIssuedAt(new Date(System.currentTimeMillis())) // время создания токена (клейм)
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // дата истечения срока действия (клейм)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // подписываем токен
                .compact();
    }

    /**
     * Проверяет валидность токена.
     * <p>
     * Этот метод проверяет, соответствует ли имя пользователя, закодированное в токене,
     * имени пользователя из предоставленных пользовательских данных, и не истек ли срок
     * действия токена.
     * <p>
     *
     * @param token       Строка токена для проверки.
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @return true, если токен действителен, иначе false.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Проверяет валидность рефрешь токена, перед тем как делать с ним какие либо действия
     *
     * @param token строка с токеном JWT
     * @throws InvalidTokenException который собирает в себе:
     *                               <p>
     *                               {@link UnsupportedJwtException}  не соответствует формату JWT;
     *                               <p>
     *                               {@link MalformedJwtException}    поврежденный JWT
     *                               <p>
     *                               {@link  SignatureException}       неверная подпись
     *                               <p>
     *                               {@link ExpiredJwtException}      время действия JWT вышло
     *                               <p>
     *                               {@link IllegalArgumentException} передан null/пустая строка/строка из пробелов
     */
    public void validateRefreshToken(String token) {
        try {
            extractAllClaims(token);
            //из метода parseClaimsJws вот это всё вылетает, это всё Runtime
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException |
                 ExpiredJwtException | IllegalArgumentException ex) {
            log.warn("Refresh token is invalid: {}", ex.getMessage());
            throw new InvalidTokenException(ex.getMessage());
        }
    }

    /**
     * Проверяет, истек ли срок действия токена.
     * <p>
     * Метод сравнивает дату истечения токена с текущей датой.
     * <p>
     *
     * @param token Строка JWT токена для проверки.
     * @return true, если срок действия токена истек, иначе false.
     */
    private boolean isTokenExpired(String token) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлекает дату истечения срока действия токена.
     * <p>
     * Метод возвращает дату истечения срока действия JWT токена.
     * <p>
     *
     * @param token Строка JWT токена.
     * @return Дата истечения срока действия токена.
     */
    private Date extractExpiration(String token) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return extractClaim(token, Claims::getExpiration);
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
    private Claims extractAllClaims(String token) { // извлекает все клэймы у токена (поля)
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) // устанавливаем ключ подписи
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
    private Key getSignInKey() { // декодирует секретный ключ из Base64 и возвращает объект Key для использования при подписи токена
        log.info("Method {} was invoked", MethodLog.getMethodName());
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
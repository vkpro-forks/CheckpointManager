package ru.ac.checkpointmanager.configuration;

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
import ru.ac.checkpointmanager.utils.MethodLog;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}") // 1 день
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")  // 7 дней
    private long refreshExpiration;

    public String extractUsername(String token) { // извлекаем sub из токена
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRole(String token) { // извлекаем роль из токена
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return extractAllClaims(token).get("role", List.class);
    }

    /* Это обобщенный метод, который принимает два аргумента:
     * token - строка, представляющая токен JWT, из которого нужно извлечь клейм (поле).
     * claimsResolver - функция, принимающая объект Claims (класс из библиотеки JJWT, представляющий все клеймы токена)
     * и возвращающая значение типа T. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) { // используется при регистрации, аунтетификации и обновления токена в ауф-сервисе
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) { // для метода выше
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) { // при регистрации и аунтетификации
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        List<String> rolesList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        extraClaims.put("role", rolesList); // добавляем в клеймы токена роль юзера

        return Jwts
                .builder()
                .setClaims(extraClaims) // устанавливает доп клеймы, которые мы хотим вшить в токен
                .setSubject(userDetails.getUsername()) // sub == username
                .setIssuedAt(new Date(System.currentTimeMillis())) // время создания токена (клейм)
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // дата истечения срока действия (клейм)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // подписываем токен
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("Method {}, User {}", MethodLog.getMethodName(), userDetails.getUsername());
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) { // извлекает все клэймы у токена (поля)
        log.info("Method {} was invoked", MethodLog.getMethodName());
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) // устанавливаем ключ подписи
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() { // декодирует секретный ключ из Base64 и возвращает объект Key для использования при подписи токена
        log.info("Method {} was invoked", MethodLog.getMethodName());
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    // hmacShaKeyFor создает секретный ключ для HMAC (Hash-based Message Authentication Code) с использованием алгоритма SHA
    // (Secure Hash Algorithm). HMAC - это специфический тип кода аутентификации сообщений, который использует криптографическую
    // хеш-функцию в сочетании с секретным ключом
}


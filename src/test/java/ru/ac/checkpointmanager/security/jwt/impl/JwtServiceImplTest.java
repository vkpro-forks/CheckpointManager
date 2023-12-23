package ru.ac.checkpointmanager.security.jwt.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;

class JwtServiceImplTest {

    JwtServiceImpl jwtService;

    @BeforeEach
    void init() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secretKey", TestUtils.getKey());
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60000000);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 60000000);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotThrowExceptionIfSubjectNullOrEmpty(String username) {
        String jwt = TestUtils.getJwt(60000, username, List.of("ADMIN"), false, true);

        Assertions.assertThatNoException().isThrownBy(() -> jwtService.extractAllClaims(jwt));
    }

}

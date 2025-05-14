package site.caboomlog.tokenservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import site.caboomlog.tokenservice.dto.TokenRefreshResponse;
import site.caboomlog.tokenservice.exception.InvalidTokenException;
import site.caboomlog.tokenservice.util.JwtTokenUtils;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    JwtTokenUtils jwtTokenUtils;

    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void refreshToken() {
        // given
        String testMbUuid = UUID.randomUUID().toString();
        Mockito.when(jwtTokenUtils.isTokenValid(anyString())).thenReturn(true);
        Mockito.when(jwtTokenUtils.getMbUuidFromToken(anyString())).thenReturn(testMbUuid);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(anyString())).thenReturn("test_refresh_token");
        Mockito.when(jwtTokenUtils.generateAccessToken(anyString())).thenReturn("test_new_access_token");

        // when
        TokenRefreshResponse response = refreshTokenService.refreshToken("test_refresh_token");

        // then
        Assertions.assertEquals("test_new_access_token", response.getAccessToken());
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).isTokenValid(anyString());
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
    void refreshToken_Invalid() {
        // given
        Mockito.when(jwtTokenUtils.isTokenValid(anyString())).thenReturn(false);

        // when & then
        Assertions.assertThrows(InvalidTokenException.class,
                () -> refreshTokenService.refreshToken("test_expired_refresh_token"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - refresh token 다른 경우")
    void refreshToken_Mismatch() {
        // given
        String testMbUuid = UUID.randomUUID().toString();
        Mockito.when(jwtTokenUtils.isTokenValid(anyString())).thenReturn(true);
        Mockito.when(jwtTokenUtils.getMbUuidFromToken(anyString())).thenReturn(testMbUuid);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(anyString())).thenReturn("test_different_refresh_token");

        // when
        Assertions.assertThrows(InvalidTokenException.class,
                () -> refreshTokenService.refreshToken("test_expired_refresh_token"));
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).isTokenValid(anyString());
        Mockito.verify(jwtTokenUtils, Mockito.times(0)).generateAccessToken(anyString());
    }
}
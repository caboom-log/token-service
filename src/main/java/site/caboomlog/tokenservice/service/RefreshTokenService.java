package site.caboomlog.tokenservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import site.caboomlog.tokenservice.util.JwtTokenUtils;
import site.caboomlog.tokenservice.dto.TokenRefreshResponse;
import site.caboomlog.tokenservice.exception.InvalidTokenException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value(("${token.expiration_time.refresh}"))
    private Long refreshExpirationTime;
    private static final String REFRESH_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenUtils jwtTokenUtils;

    public void store(String mbUuid, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + mbUuid,
                refreshToken,
                Duration.ofMillis(refreshExpirationTime)
        );
    }

    private String getToken(String mbUuid) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + mbUuid);
    }

    public void deleteToken(String refreshToken) {
        if (jwtTokenUtils.isTokenValid(refreshToken)) {
            String mbUuid = jwtTokenUtils.getMbUuidFromToken(refreshToken);
            redisTemplate.delete(REFRESH_PREFIX + mbUuid);
        }
    }

    public TokenRefreshResponse refreshToken(String refreshToken) {
        if (!jwtTokenUtils.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String mbUuid = jwtTokenUtils.getMbUuidFromToken(refreshToken);
        String storedRefreshToken = getToken(mbUuid);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new InvalidTokenException("Refresh token mismatch");
        }
        String newAccessToken = jwtTokenUtils.generateAccessToken(mbUuid);
        return new TokenRefreshResponse(newAccessToken);
    }
}

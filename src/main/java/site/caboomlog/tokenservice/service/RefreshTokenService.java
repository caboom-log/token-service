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

    public void store(Long mbNo, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + mbNo,
                refreshToken,
                Duration.ofMillis(refreshExpirationTime)
        );
    }

    private String getToken(Long mbNo) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + mbNo);
    }

    public void deleteToken(String refreshToken) {
        if (jwtTokenUtils.isTokenValid(refreshToken)) {
            Long mbNo = jwtTokenUtils.getMbNoFromToken(refreshToken);
            redisTemplate.delete(REFRESH_PREFIX + mbNo);
        }
    }

    public TokenRefreshResponse refreshToken(String refreshToken) {
        if (!jwtTokenUtils.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        Long mbNo = jwtTokenUtils.getMbNoFromToken(refreshToken);
        String storedRefreshToken = getToken(mbNo);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new InvalidTokenException("Refresh token mismatch");
        }
        String newAccessToken = jwtTokenUtils.generateAccessToken(mbNo);
        return new TokenRefreshResponse(newAccessToken);
    }
}

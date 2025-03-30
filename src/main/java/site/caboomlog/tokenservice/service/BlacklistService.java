package site.caboomlog.tokenservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import site.caboomlog.tokenservice.util.JwtTokenUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenUtils jwtTokenUtils;

    private void addToBlacklist(String accessToken, long remain) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                Duration.ofMillis(remain)
        );
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }

    public void logout(String accessToken) {
        long remain = jwtTokenUtils.getRemainingTime(accessToken);
        addToBlacklist(accessToken, remain);
    }

}

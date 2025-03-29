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
import site.caboomlog.tokenservice.util.JwtTokenUtils;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {

    @Mock
    JwtTokenUtils jwtTokenUtils;

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    BlacklistService blacklistService;

    @Test
    @DisplayName("로그아웃 성공")
    void logout() {
        // given
        Mockito.when(jwtTokenUtils.getRemainingTime(anyString()))
                .thenReturn(20000L);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doNothing().when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));

        // when & then
        Assertions.assertDoesNotThrow(() -> blacklistService.logout("test_access_token"));
    }
}
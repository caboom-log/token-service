package site.caboomlog.tokenservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import site.caboomlog.tokenservice.dto.TokenIssueResponse;
import site.caboomlog.tokenservice.dto.TokenValidationResponse;
import site.caboomlog.tokenservice.util.JwtTokenUtils;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock
    JwtTokenUtils jwtTokenUtils;

    @InjectMocks
    AccessTokenService accessTokenService;

    @Test
    @DisplayName("토큰 발급 성공")
    void issueToken() {
        // given
        Mockito.when(jwtTokenUtils.generateAccessToken(anyLong()))
                .thenReturn("test_access_token");
        Mockito.when(jwtTokenUtils.generateRefreshToken(anyLong()))
                .thenReturn("test_refresh_token");

        // when
        TokenIssueResponse response = accessTokenService.issueToken(1L);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals("test_access_token", response.getAccessToken()),
                () -> Assertions.assertEquals("test_refresh_token", response.getRefreshToken())
        );
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).generateAccessToken(anyLong());
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).generateRefreshToken(anyLong());
    }

    @Test
    @DisplayName("토큰 검증 성공")
    void validateToken() {
        // given
        Mockito.when(jwtTokenUtils.isTokenValid(anyString())).thenReturn(true);
        Mockito.when(jwtTokenUtils.getMbNoFromToken(anyString())).thenReturn(1L);

        // when
        TokenValidationResponse response = accessTokenService.validateToken("test_access_token");

        // then
        Assertions.assertAll(
                () -> Assertions.assertTrue(response.isValid()),
                () -> Assertions.assertEquals(1L, response.getMbNo())
        );
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).isTokenValid(anyString());
    }

    @Test
    @DisplayName("토큰 검증 실패")
    void validateTokenFail_Invalid() {
        // given
        Mockito.when(jwtTokenUtils.isTokenValid(anyString())).thenReturn(false);

        // when
        TokenValidationResponse response = accessTokenService.validateToken("test_access_token");

        // then
        Assertions.assertFalse(response.isValid());
        Mockito.verify(jwtTokenUtils, Mockito.times(1)).isTokenValid(anyString());
    }
}
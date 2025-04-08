package site.caboomlog.tokenservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import site.caboomlog.tokenservice.dto.TokenIssueResponse;
import site.caboomlog.tokenservice.dto.TokenRefreshResponse;
import site.caboomlog.tokenservice.dto.TokenValidationResponse;
import site.caboomlog.tokenservice.exception.InvalidTokenException;
import site.caboomlog.tokenservice.service.AccessTokenService;
import site.caboomlog.tokenservice.service.BlacklistService;
import site.caboomlog.tokenservice.service.RefreshTokenService;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TokenController.class)
class TokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AccessTokenService accessTokenService;
    @MockBean
    RefreshTokenService refreshTokenService;
    @MockBean
    BlacklistService blacklistService;

    @Test
    @DisplayName("토큰 발급 요청 성공")
    void issueToken() throws Exception {
        // given
        String testUuid = UUID.randomUUID().toString();
        Map<String, String> request = Map.of("mbUuid", testUuid);
        TokenIssueResponse response = new TokenIssueResponse(
                "test_access_token",
                "test_refresh_token");
        Mockito.when(accessTokenService.issueToken(anyString()))
                                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/token/issue")
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()));
    }

    @Test
    @DisplayName("토큰 발급 요청 실패 - 잘못된 요청")
    void issueTokenFail_BadRequest() throws Exception {
        mockMvc.perform(post("/token/issue")
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .content("{\"mbUuid\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("MbUuid cannot be null or empty"));
    }

    @Test
    @DisplayName("토큰 검증 요청")
    void validateToken() throws Exception {
        // given
        Map<String, String> request = Map.of("token", "test_access_token");
        Mockito.when(blacklistService.isBlacklisted(anyString()))
                .thenReturn(false);
        String testMbUuid = UUID.randomUUID().toString();
        TokenValidationResponse response = new TokenValidationResponse(true, testMbUuid);
        Mockito.when(accessTokenService.validateToken("test_access_token"))
                        .thenReturn(response);

        // when & then
        mockMvc.perform(post("/token/validate")
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .header(ACCEPT, APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.mbUuid").value(testMbUuid));
    }

    @Test
    @DisplayName("토큰 검증 요청 실패 - 잘못된 요청")
    void validateTokenFail_BadRequest() throws Exception {
        // given
        Map<String, String> request = Map.of("token", "");

        // when & then
        mockMvc.perform(post("/token/validate")
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .header(ACCEPT, APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("토큰 검증 요청 실패 - 블랙리스트에 들어가 있는 토큰")
    void validateTokenFail_Blacklist() throws Exception {
        // given
        Map<String, String> request = Map.of("token", "test_blacklist_access_token");
        Mockito.when(blacklistService.isBlacklisted(anyString()))
                        .thenReturn(true);

        // when & then
        mockMvc.perform(post("/token/validate")
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .header(ACCEPT, APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("토큰 검증 요청 실패 - 유효하지 않은 토큰")
    void validateTokenFail_Invalid() throws Exception {
        // given
        Map<String, String> request = Map.of("token", "test_blacklist_access_token");
        Mockito.when(blacklistService.isBlacklisted(anyString()))
                .thenReturn(false);
        Mockito.when(accessTokenService.validateToken(anyString()))
                        .thenReturn(new TokenValidationResponse(false, null));

        // when & then
        mockMvc.perform(post("/token/validate")
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .header(ACCEPT, APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refreshToken() throws Exception {
        // given
        Mockito.when(refreshTokenService.refreshToken(anyString()))
                .thenReturn(new TokenRefreshResponse("test_new_access_token"));

        // when & then
        mockMvc.perform(post("/token/refresh")
                        .cookie(new Cookie("refreshToken", "test_refresh_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test_new_access_token"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 잘못된 요청")
    void refreshTokenFail_BadRequest() throws Exception {
        mockMvc.perform(post("/token/refresh")
                        .cookie(new Cookie("refreshToken", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage")
                        .value("Refresh token cannot be null or empty"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 쿠키 없이 요청")
    void refreshTokenFail_WithNoCookie() throws Exception {
        mockMvc.perform(post("/token/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
    void refreshTokenFail_Invalid() throws Exception {
        // given
        Mockito.when(refreshTokenService.refreshToken(anyString()))
                .thenThrow(new InvalidTokenException("Invalid refresh token"));

        // when & then
        mockMvc.perform(post("/token/refresh")
                        .cookie(new Cookie("refreshToken", "test_refresh_token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("Invalid refresh token"));
    }

    @Test
    @DisplayName("로그아웃시 토큰 삭제 성공")
    void logout() throws Exception {
        // given
        Mockito.when(refreshTokenService.refreshToken(anyString()))
                .thenThrow(new InvalidTokenException("Invalid refresh token"));
        Mockito.doNothing().when(blacklistService).logout(anyString());
        Mockito.doNothing().when(refreshTokenService).deleteToken(anyString());

        // when & then
        mockMvc.perform(post("/token/logout")
                        .header(AUTHORIZATION, "Bearer test_access_token"))
                .andExpect(status().isOk());
    }

}
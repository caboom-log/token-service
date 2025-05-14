package site.caboomlog.tokenservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.tokenservice.exception.BadRequestException;
import site.caboomlog.tokenservice.service.AccessTokenService;
import site.caboomlog.tokenservice.service.BlacklistService;
import site.caboomlog.tokenservice.service.RefreshTokenService;
import site.caboomlog.tokenservice.dto.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    @PostMapping("/token/issue")
    public ResponseEntity<TokenIssueResponse> issueToken(@RequestBody TokenIssueRequest request) {
        String mbUuid = request.getMbUuid();
        if (mbUuid == null || mbUuid.isBlank()) {
            throw new BadRequestException("MbUuid cannot be null or empty");
        }
        TokenIssueResponse response = accessTokenService.issueToken(mbUuid);
        refreshTokenService.store(mbUuid, response.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        String token = request.getToken();
        if (token == null || token.isBlank()) {
            log.debug("Token is null or empty");
            return ResponseEntity.ok(new TokenValidationResponse(false, null));
        }
        if (blacklistService.isBlacklisted(token)) {
            log.debug("Token is blacklisted");
            return ResponseEntity.ok(new TokenValidationResponse(false, null));
        }

        TokenValidationResponse response = accessTokenService.validateToken(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Refresh token cannot be null or empty");
        }
        TokenRefreshResponse response = refreshTokenService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            blacklistService.logout(token);
            refreshTokenService.deleteToken(token);
        }
        return ResponseEntity.ok().build();
    }
}

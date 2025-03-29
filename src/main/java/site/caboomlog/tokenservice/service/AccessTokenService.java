package site.caboomlog.tokenservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.caboomlog.tokenservice.util.JwtTokenUtils;
import site.caboomlog.tokenservice.dto.TokenIssueResponse;
import site.caboomlog.tokenservice.dto.TokenValidationResponse;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final JwtTokenUtils jwtTokenUtils;

    public TokenIssueResponse issueToken(Long mbNo) {
        String accessToken = jwtTokenUtils.generateAccessToken(mbNo);
        String refreshToken = jwtTokenUtils.generateRefreshToken(mbNo);
        return new TokenIssueResponse(accessToken, refreshToken);
    }

    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();
        boolean tokenValid = jwtTokenUtils.isTokenValid(token);
        if (tokenValid) {
            response.setValid(true);
            response.setMbNo(jwtTokenUtils.getMbNoFromToken(token));
        } else {
            response.setValid(false);
        }
        return response;
    }

}

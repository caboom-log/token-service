package site.caboomlog.tokenservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenRefreshResponse {
    private String accessToken;
}

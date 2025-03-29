package site.caboomlog.tokenservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenIssueResponse {
    private String accessToken;
    private String refreshToken;
}

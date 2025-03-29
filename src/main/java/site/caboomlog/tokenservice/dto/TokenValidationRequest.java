package site.caboomlog.tokenservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TokenValidationRequest {
    private String token;
}

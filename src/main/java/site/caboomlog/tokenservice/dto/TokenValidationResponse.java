package site.caboomlog.tokenservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
public class TokenValidationResponse {
    private boolean valid;
    private Long mbNo;
}

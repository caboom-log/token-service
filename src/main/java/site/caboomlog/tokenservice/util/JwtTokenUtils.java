package site.caboomlog.tokenservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenUtils {

    @Value("${token.secret}")
    private String secret;
    @Value("${token.expiration_time.access}")
    private Long accessExpirationTime;
    @Value(("${token.expiration_time.refresh}"))
    private Long refreshExpirationTime;

    public String generateAccessToken(String mbUuid) {
        return generateToken(mbUuid, accessExpirationTime);
    }

    public String generateRefreshToken(String mbUuid) {
        return generateToken(mbUuid, refreshExpirationTime);
    }

    private String generateToken(String mbUuid, Long expirationTime) {

        Claims claims = Jwts.claims().setSubject(mbUuid);

        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(mbUuid)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Expired or Invalid token", e);
            return false;
        }
    }

    public String getMbUuidFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public long getRemainingTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long nowMillis = System.currentTimeMillis();
            return expiration.getTime() - nowMillis;
        } catch (Exception e) {
            log.warn("Failed to get remaining time from token", e);
            return 0;
        }
    }

}

package com.sparklenote.user.jwt;

import com.sparklenote.domain.enumType.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTUtil {

    private SecretKey secretKey;

    public JWTUtil(@Value("${jwt.secret}") String secret) {
        log.info("Initializing JWTUtil with secret");
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }

    public String createAccessToken(String username, Role role, Long expiredMs) {
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String username, Long expiredMs) {
        return Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            // 기타 JWT 관련 예외 처리 (필요시)
            return true;
        }
    }

    public boolean isValidToken(String token) {
        try {
            // 서명 검증 및 토큰 파싱
            Jwts.parser().verifyWith(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            log.error("토큰이 만료되었습니다.");
            return false;
        } catch (JwtException e) {
            log.error("유효하지 않은 토큰입니다.");
            return false;
        }
    }


}
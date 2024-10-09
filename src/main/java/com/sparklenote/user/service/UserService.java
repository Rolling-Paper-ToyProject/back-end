package com.sparklenote.user.service;

import com.sparklenote.domain.enumType.Role;
import com.sparklenote.user.dto.request.TokenRequestDTO;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    private final JWTUtil jwtUtil;

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 토큰을 재발급 하는 메소드
     */
    public TokenResponseDTO refreshToken(TokenRequestDTO tokenRequestDTO) {
        String refreshToken = tokenRequestDTO.getRefreshToken();
        // 리프레시 토큰 검증
        if (!jwtUtil.isValidToken(refreshToken)) {
            log.error("Refresh Token이 유효하지 않습니다.");
        }
        // 리프레시 토큰에서 사용자 정보 추출
        String username = jwtUtil.getUsername(refreshToken);
        // 새로운 엑세스 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(username, Role.TEACHER, accessTokenExpiration);
        return new TokenResponseDTO(newAccessToken);
    }

    /**
     * 쿠키를 만드는 메소드
     */
    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(3600); // 1시간 (초 단위)
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    /**
     * 로그아웃 시 토큰을 블랙리스트에 저장
     */
    public void handleLogout(String refreshToken) {
        // 리프레시 토큰의 남은 유효 시간을 가져와 해당 시간으로 유효기간을 설정
        long expiration = 0; // 토큰의 만료 시간을 가져오는 메소드
        redisTemplate.opsForValue().set(refreshToken, "loggedOut", expiration, TimeUnit.SECONDS);
    }

}

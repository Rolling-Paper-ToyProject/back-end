package com.sparklenote.user.service;

import com.sparklenote.domain.enumType.Role;
import com.sparklenote.user.dto.request.TokenRequestDTO;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    private final JWTUtil jwtUtil;

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

    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(3600); // 예시: 1시간 (초 단위)
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}

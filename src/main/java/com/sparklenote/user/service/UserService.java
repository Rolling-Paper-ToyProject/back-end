package com.sparklenote.user.service;

import com.sparklenote.common.exception.UserException;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.user.dto.request.TokenRequestDTO;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.sparklenote.common.error.code.UserErrorCode.TOKEN_IS_NOT_VALID;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    private final JWTUtil jwtUtil;

    /**
     * 토큰을 재발급 하는 메소드
     */
    public TokenResponseDTO refreshToken(TokenRequestDTO tokenRequestDTO) {
        String refreshToken = tokenRequestDTO.getRefreshToken();
        // 리프레시 토큰 검증
        if (!jwtUtil.isValidToken(refreshToken)) {
            throw new UserException(TOKEN_IS_NOT_VALID);
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
}

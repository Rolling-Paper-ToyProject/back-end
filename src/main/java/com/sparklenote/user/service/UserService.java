package com.sparklenote.user.service;

import com.sparklenote.common.exception.UserException;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.user.dto.request.TokenRequestDTO;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.dto.response.UserInfoResponseDTO;
import com.sparklenote.user.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.sparklenote.common.error.code.UserErrorCode.TOKEN_IS_NOT_VALID;
import static com.sparklenote.common.error.code.UserErrorCode.USER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    private final JWTUtil jwtUtil;

    /**
     * 토큰을 재발급 하는 메소드
     */
    public TokenResponseDTO refreshToken(String refreshToken) {
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

    public UserInfoResponseDTO getUserInfo(String authorizationHeader) {

        String token = extractToken(authorizationHeader);

        boolean validToken = jwtUtil.isValidToken(token);

        if (!validToken) {
            throw new UserException(TOKEN_IS_NOT_VALID);
        }

        String username = jwtUtil.getUsername(token);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new UserException(USER_NOT_FOUND);
        }

        Long userId = optionalUser.get().getId();
        String name = optionalUser.get().getName();

        UserInfoResponseDTO userInfoResponseDTO = UserInfoResponseDTO.builder()
                .userId(userId)
                .name(name)
                .build();
        return userInfoResponseDTO;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new UserException(TOKEN_IS_NOT_VALID);
    }
}


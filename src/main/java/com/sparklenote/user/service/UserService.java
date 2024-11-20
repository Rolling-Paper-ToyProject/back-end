package com.sparklenote.user.service;

import com.sparklenote.common.exception.UserException;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.user.dto.response.TokenResponseDTO;
import com.sparklenote.user.dto.response.UserInfoResponseDTO;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.sparklenote.common.error.code.UserErrorCode.TOKEN_IS_NOT_VALID;
import static com.sparklenote.common.error.code.UserErrorCode.USER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
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

        // DB에서 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 새로운 엑세스 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(
                username,
                user.getName(),
                Role.TEACHER,
                accessTokenExpiration
        );
        return new TokenResponseDTO(newAccessToken);
    }

    public UserInfoResponseDTO getUserInfo() {
        String name = getCustomOAuth2User();
        UserInfoResponseDTO responseDTO = UserInfoResponseDTO.builder()
                .name(name)
                .role(Role.TEACHER.name())
                .build();
        return responseDTO;
    }

    private static String getCustomOAuth2User() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomOAuth2User customOAuth2User)) {
            return null;
        }
        String name = customOAuth2User.getName();

        return name;
    }
}

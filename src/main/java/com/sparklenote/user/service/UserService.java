package com.sparklenote.user.service;

import com.sparklenote.common.exception.UserException;
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

    public UserInfoResponseDTO getUserInfo() {
        log.debug("getUserInfo method called");

        String name = getCustomOAuth2User();
        log.debug("Retrieved name from CustomOAuth2User: {}", name);

        UserInfoResponseDTO responseDTO = UserInfoResponseDTO.builder()
                .name(name)
                .build();

        log.debug("Created UserInfoResponseDTO with name: {}", responseDTO.getName());
        return responseDTO;
    }

    private static String getCustomOAuth2User() {
        log.debug("getCustomOAuth2User method called");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication from SecurityContext: {}", authentication);

        if (authentication == null) {
            log.error("Authentication is null");
            return null;
        }

        Object principal = authentication.getPrincipal();
        log.debug("Principal class type: {}", principal.getClass().getName());

        if (!(principal instanceof CustomOAuth2User)) {
            log.error("Principal is not CustomOAuth2User");
            return null;
        }

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
        log.debug("Successfully cast to CustomOAuth2User");

        String name = customOAuth2User.getName();
        log.debug("Retrieved name from CustomOAuth2User in getCustomOAuth2User: {}", name);

        return name;
    }
}

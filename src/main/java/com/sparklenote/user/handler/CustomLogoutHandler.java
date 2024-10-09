package com.sparklenote.user.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 클라이언트로부터 리프레시 토큰을 받아와서 처리
        String refreshToken = getRefreshTokenFromRequest(request);

        if (refreshToken != null) {
            // Redis에 리프레시 토큰을 0초 유효기간으로 설정하여 즉시 만료
            redisTemplate.opsForValue().set(refreshToken, "loggedOut", 0, TimeUnit.SECONDS);
        }
    }

    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        // 리프레시 토큰을 쿠키에서 추출하는 로직 (예시)
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("RefreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

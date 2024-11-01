package com.sparklenote.user.handler;


import com.sparklenote.domain.enumType.Role;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Value("${jwt.accessExpiration}") // 30분
    private Long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}") // 1일
    private Long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();

        String accessToken = jwtUtil.createAccessToken(username, Role.TEACHER, accessTokenExpiration);
        String refreshToken = jwtUtil.createRefreshToken(username, refreshTokenExpiration);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("RefreshToken", refreshToken);
        response.sendRedirect("http://localhost:3000/oauth/callback");
    }
}
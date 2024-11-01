package com.sparklenote.user.jwt;

import com.sparklenote.domain.enumType.Role;
import com.sparklenote.user.dto.response.UserResponseDTO;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 소셜 로그인 경로에 대한 요청은 JWT 필터링을 하지 않음
        // 필터링을 무시할 경로 추가
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/login") ||
                requestURI.startsWith("/oauth2/authorization") ||
                requestURI.startsWith("/login/oauth2/code") ||
                requestURI.startsWith("/roll/join/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        // 헤더에 Authorization 값이 없으면 필터 체인 계속 진행
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 'Bearer ' 접두사 제거 후 실제 토큰만 추출
        String token = authorizationHeader.substring(7);
        // 토큰 소멸 시간, 유효성 검증
        if (jwtUtil.isExpired(token) || !jwtUtil.isValidToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        String roleName = jwtUtil.getRole(token);
        Role role = Role.valueOf(roleName);

        // userDTO를 생성하여 값 set
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername(username);
        userResponseDTO.setRole(role);

        // UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userResponseDTO);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
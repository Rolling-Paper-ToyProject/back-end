package com.sparklenote.user.jwt;

import com.sparklenote.domain.enumType.Role;
import com.sparklenote.student.dto.response.StudentResponseDTO;
import com.sparklenote.student.userDetails.CustomStudentDetails;
import com.sparklenote.user.dto.response.UserResponseDTO;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import com.sparklenote.student.userDetails.CustomStudentDetails;
import com.sparklenote.student.dto.response.StudentResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;


import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 소셜 로그인 경로에 대한 요청은 JWT 필터링을 하지 않음
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/login") ||
                requestURI.startsWith("/oauth2/authorization") ||
                requestURI.startsWith("/login/oauth2/code")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);
        if (jwtUtil.isExpired(token) || !jwtUtil.isValidToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 정보 획득
        String username = jwtUtil.getUsername(token);
        String roleName = jwtUtil.getRole(token);
        String name = jwtUtil.getName(token);
        Role role = Role.valueOf(roleName);

        // 역할에 따라 다른 인증 객체 생성
        Authentication authToken;
        if (role == Role.TEACHER) {  // 선생님인 경우

            UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                    .username(username)
                    .name(name)  // name 설정 확인
                    .role(role)
                    .build();

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(userResponseDTO);
            authToken = new UsernamePasswordAuthenticationToken(
                    customOAuth2User,
                    null,
                    customOAuth2User.getAuthorities()
            );
        } else {  // 학생인 경우
            StudentResponseDTO studentResponseDTO = StudentResponseDTO.builder()
                    .studentId(Long.parseLong(username))
                    .name(username)
                    .role(role)
                    .password("")
                    .build();

            CustomStudentDetails customStudentDetails = new CustomStudentDetails(studentResponseDTO);
            authToken = new UsernamePasswordAuthenticationToken(
                    customStudentDetails,
                    null,
                    customStudentDetails.getAuthorities()
            );
        }

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
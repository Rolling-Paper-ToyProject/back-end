package com.sparklenote.common.config;

import com.sparklenote.user.handler.CustomLogoutHandler;
import com.sparklenote.user.handler.CustomSuccessHandler;
import com.sparklenote.user.jwt.JWTFilter;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomLogoutHandler customLogoutHandler;
    private final JWTUtil jwtUtil;

    @Bean
    @Order(1)  // shutdown endpoint에 대해서 가장 먼저 필터체인 적용
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/shutdown")  // /actuator/shutdown 경로에 대해서만 필터 체인 적용
                .authorizeHttpRequests((auth) -> auth
                        .anyRequest().hasRole("ACTUATOR_ADMIN")  // ACTUATOR_ADMIN 역할만 접근 가능
                )
                .httpBasic(httpBasic -> httpBasic
                        .realmName("ACTUATOR")  // 기본 인증 영역 이름 설정
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);


                        return configuration;
                    }
                }));

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //Form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

//JWTFilter 추가
        http
                .addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        //oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated());

        //세션 설정 : STATELSS (추후 학생들의 로그인 방식이 정확해지면 변경 예정)
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 로그아웃 설정 추가
        http
                .logout((logout) -> logout
                .logoutUrl("/logout")  // 로그아웃 엔드포인트 설정
                .addLogoutHandler(customLogoutHandler)  // 커스텀 로그아웃 핸들러 추가
                .logoutSuccessUrl("http://localhost:3000/login")  // 로그아웃 성공 후 리다이렉트할 URL (지금은 메인페이지의 주소가 정확하지 않아서 로그인 페이지로 리다이렉트 설정 해놓았음)
                 // logoutSuccessUrl("/index.html") -> 처럼 설정하면 됨
                .invalidateHttpSession(true)  // 세션 무효화
                .deleteCookies("Authorization", "RefreshToken")  // 로그아웃 시 쿠키 삭제
                .permitAll());

        return http.build();
    }

    /**
     * 권한 없이 허용하는 endpoint
     */

    private RequestMatcher[] permitAllRequestMatchers() {
        List<RequestMatcher> requestMatchers = List.of(
          antMatcher(POST, "/user/login")
        );
        return requestMatchers.toArray(RequestMatcher[]::new);
    }

}
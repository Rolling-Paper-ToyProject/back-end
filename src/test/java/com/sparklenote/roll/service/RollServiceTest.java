package com.sparklenote.roll.service;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RollServiceTest {

    @Mock
    private RollRepository rollRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UrlGenerator urlGenerator;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private ClassCodeGenerator classCodeGenerator;

    @InjectMocks
    private RollService rollService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 위에서 설정한 Mock 객체들을 전부 초기화 (안하면 null)

        // SecurityContext, Authentication을 Mock처리
        SecurityContext securityContext = mock(SecurityContext.class); // 스프링 시큐리티의 인증 전반의 생태계 느낌
        Authentication authentication = mock(Authentication.class); // 유저 개개인의 인증정보

        // CustomOAuth2 객체를 Mock으로 처리, username 값을 넣어줌
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUsername()).thenReturn("testUsername");

        when(authentication.getPrincipal()).thenReturn(customOAuth2User); // 유저의 정보를 요청할 때는 mock 처리해 둔 customOAuth2User 객체를 반환
        when(securityContext.getAuthentication()).thenReturn(authentication); // 현재 인증정보를 가져오는 로직

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Roll 생성 - 단위테스트")
    void createRoll_success() {

        // GIVEN
        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");
        int classCode = ClassCodeGenerator.generateClassCode();
        String url = urlGenerator.generateUrl();

        User mockUser = User.builder()
                .username("testUsername")
                .build();

        Roll roll = Roll.fromRollCreateDto(requestDto, classCode, url, mockUser);

        // Mock 설정
        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(mockUser));
        given(urlGenerator.generateUrl()).willReturn(url);
        given(rollRepository.save(any(Roll.class))).willReturn(roll);

        // WHEN : Roll 생성 메소드 호출
        RollResponseDTO response = rollService.createRoll(requestDto); // 안에서 save와 반환까지 다 완료한 상태

        // THEN : 값 검증
        assertThat(response.getUserId()).isEqualTo(mockUser.getId());
        assertThat(response.getClassCode()).isEqualTo(classCode);
        assertThat(response.getUrl()).isEqualTo(url);
        assertThat(response.getRollName()).isEqualTo(requestDto.getRollName());
    }
}

package com.sparklenote.roll.service;

import com.sparklenote.common.exception.RollException;
import com.sparklenote.common.exception.UserException;
import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.request.RollUpdateRequestDto;
import com.sparklenote.roll.dto.response.RollJoinResponseDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sparklenote.common.error.code.RollErrorCode.CLASS_CODE_GENERATION_ERROR;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Order(n)에 따라 순서 보장
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
    private PaperService paperService;

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
    @Order(1)
    @DisplayName("Roll 생성 - 성공")
    void createRoll_success() {

        // GIVEN
        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");
        int classCode = ClassCodeGenerator.generateClassCode();
        String url = urlGenerator.generateUrl();

        User mockUser = User.builder()
                .username("testUsername")
                .build();

        Roll roll = Roll.createRollFromDto(requestDto, classCode, url, mockUser);


        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(mockUser)); // username이 같으면 같은 유저로 판단
        given(urlGenerator.generateUrl()).willReturn(url); // 같은 url이면 같은 Roll이라고 판단
        given(rollRepository.save(any(Roll.class))).willReturn(roll); // 참조값까지 같은 필요 없으니까 러프하게 처리

        // WHEN : Roll 생성 메소드 호출
        RollResponseDTO response = rollService.createRoll(requestDto); // 안에서 save와 반환까지 다 완료한 상태

        // THEN : 값 검증
        assertThat(response.getUserId()).isEqualTo(mockUser.getId());
        assertThat(response.getClassCode()).isEqualTo(classCode);
        assertThat(response.getUrl()).isEqualTo(url);
        assertThat(response.getRollName()).isEqualTo(requestDto.getRollName());
    }

    @Test
    @Order(2)
    @DisplayName("Roll 생성 - 실패 (사용자 조회 실패)")
    void createRoll_fail_username_not_found() {

        // GIVEN
        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");

        // WHEN
        given(userRepository.findByUsername("testUsername")).willReturn(Optional.empty());

        // THEN
        assertThrows(UserException.class, () -> rollService.createRoll(requestDto));
    }

    @Test
    @Order(3)
    @DisplayName("Roll 생성 - 실패 (권한 없는 사용자 - STUDENT)")
    void createRoll_fail_unauthorized_user() {

        // GIVEN
        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");

        User user = User.builder()
                .username("testUsername")
                .role(Role.STUDENT)
                .build();

        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(user));

        //WHEN & THEN
        assertThrows(RollException.class, () -> rollService.createRoll(requestDto));

    }

    @Test
    @Order(4)
    @DisplayName("Roll 생성 - 실패 (url null)")
    void createRoll_fail_url_null() {

        // GIVEN
        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");

        User user = User.builder()
                .username("testUsername")
                .build();

        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(user));
        given(urlGenerator.generateUrl()).willReturn(null);

        // WHEN & THEN
        assertThrows(NullPointerException.class, () ->rollService.createRoll(requestDto));
    }

    @Test
    @Order(5)
    @DisplayName("Roll 생성 - 실패 (classCode 발급 예외)")
    void createRoll_fail_classCode_null() {

        // GIVEN
        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");
        User user = User.builder().username("testUsername").build();
        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(user));

        // static 메소드는 일반적인 Mokito의 given 구문으로 Mocking 하기 어려움 (MockStatic 인터페이스 활용)
        try (MockedStatic<ClassCodeGenerator> mockedStatic = mockStatic(ClassCodeGenerator.class)) {
            mockedStatic.when(ClassCodeGenerator::generateClassCode)
                    .thenThrow(new RollException(CLASS_CODE_GENERATION_ERROR));

            // WHEN & THEN
            assertThrows(RollException.class, () -> rollService.createRoll(requestDto));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Roll 조회 - 성공")
    void retrieveRoll_success() {

        // GIVEN
        User user = User.builder()
                .id(1L)
                .username("testUsername")
                .build();

        Roll roll1 = Roll.builder()
                .id(1L)
                .rollName("테스트용 롤 1")
                .user(user)
                .build();

        Roll roll2 = Roll.builder()
                .id(2L)
                .rollName("테스트용 롤 2")
                .user(user)
                .build();

        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(user));
        given(rollRepository.findAllByUser(user)).willReturn(List.of(roll1, roll2));

        // WHEN
        List<RollResponseDTO> myRolls = rollService.getMyRolls();

        // THEN
        assertThat(myRolls.size()).isEqualTo(2);
        assertThat(myRolls.get(0).getRollId()).isEqualTo(1L);
        assertThat(myRolls.get(0).getRollName()).isEqualTo(roll1.getRollName());
        assertThat(myRolls.get(1).getRollId()).isEqualTo(2L);
        assertThat(myRolls.get(1).getRollName()).isEqualTo(roll2.getRollName());

    }

    @Test
    @Order(7)
    @DisplayName("Roll 조회 - 성공 (생성한 Roll이 없을 경우)")
    void retrieveRoll_success_none_roll() {

        // GIVEN
        User user = User.builder()
                .username("testUsername")
                .build();

        given(userRepository.findByUsername("testUsername")).willReturn(Optional.of(user));

        // WHEN & THEN
        assertThat(rollService.getMyRolls().size()).isEqualTo(0);
    }

    @Test
    @Order(8)
    @DisplayName("Roll 조회 - 실패 (사용자 조회 실패)")
    void retrieveRoll_fail_username_not_found() {

        // GIVEN
        given(userRepository.findByUsername("testUsername")).willReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UserException.class, () -> rollService.getMyRolls());
    }

    @Test
    @Order(9)
    @DisplayName("Roll 수정 - 성공")
    void updateRoll_success() {

        // GIVEN
        User user = User.builder()
                .id(50L)
                .build();

        Roll roll = Roll.builder()
                .id(1L)
                .rollName("수정 전 이름")
                .user(user)
                .build();

        RollUpdateRequestDto requestDto = new RollUpdateRequestDto("수정 후 roll");

        given(rollRepository.findById(1L)).willReturn(Optional.of(roll));
        given(rollRepository.save(any(Roll.class))).willReturn(roll);

        // WHEN
        RollResponseDTO responseDTO = rollService.updateRollName(1L, requestDto);

        // THEN
        assertThat(responseDTO.getRollId()).isEqualTo(1L);
        assertThat(responseDTO.getRollName()).isEqualTo(requestDto.getRollName());

    }

    @Test
    @Order(10)
    @DisplayName("Roll 수정 - 실패 (같은 이름으로 변경)")
    void updateRoll_fail_same_name() {

        // GIVEN
        RollUpdateRequestDto requestDto = new RollUpdateRequestDto("테스트용 롤");

        Roll roll = Roll.builder()
                .id(1L)
                .rollName("테스트용 롤")
                .build();

        given(rollRepository.findById(1L)).willReturn(Optional.of(roll));
        given(rollRepository.save(any(Roll.class))).willReturn(roll);

        // WHEN & THEN
        assertThrows(RollException.class, () -> rollService.updateRollName(1L, requestDto));
    }

    @Test
    @Order(11)
    @DisplayName("Roll 수정 - 실패 (Roll 조회 실패)")
    void updateRoll_fail_roll_null() {

        // GIVEN
        RollUpdateRequestDto requestDto = new RollUpdateRequestDto("테스트용 롤");

        // WHEN & THEN
        assertThrows(RollException.class, () -> rollService.updateRollName(1L, requestDto));
    }

    @Test
    @Order(12)
    @DisplayName("Roll 삭제 - 성공")
    void deleteRoll_success() {

        //GIVEN
        Roll roll = Roll.builder()
                .id(1L)
                .build();

        given(rollRepository.findById(1L)).willReturn(Optional.of(roll));

        // WHEN
        rollService.deleteRoll(1L);

        // THEN
        verify(rollRepository, times(1)).delete(roll);
    }

    @Test
    @Order(13)
    @DisplayName("Roll 삭제 - 실패 (roll이 없을 때)")
    void deleteRoll_fail_roll_not_found() {

        // GIVEN
        given(rollRepository.findById(1L)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RollException.class, () -> rollService.deleteRoll(1L));
    }

    @Test
    @Order(14)
    @DisplayName("Roll 입장(학생) - 성공")
    void joinRoll_success() {

        // GIVEN
        String url = urlGenerator.generateUrl();
        RollJoinRequestDto requestDto = new RollJoinRequestDto("테스트", 1234, 1010);

        Student student = Student.builder()
                .id(1L)
                .name("홍길동")
                .pinNumber(1010)
                .build();

        Roll roll = Roll.builder()
                .id(1L)
                .rollName("테스트용 롤")
                .classCode(1234)
                .url(url)
                .students(List.of(student))
                .build();

        List<PaperResponseDTO> papers = List.of(new PaperResponseDTO());

        given(rollRepository.findByUrl(url)).willReturn(Optional.of(roll));
        given(studentRepository.findByNameAndPinNumber("홍길동", 1010)).willReturn(Optional.of(student));
        given(studentRepository.save(any(Student.class))).willReturn(student);
        given(paperService.getPapers(roll.getId())).willReturn(papers);

        // WHEN
        RollJoinResponseDto responseDto = rollService.joinRoll(url, requestDto);

        // THEN
        assertThat(responseDto.getRollId()).isEqualTo(roll.getId());
        assertThat(responseDto.getRollName()).isEqualTo(roll.getRollName());
        assertThat(responseDto.getStudentName()).isEqualTo(student.getName());
        assertThat(responseDto.getPapers().size()).isEqualTo(papers.size());

    }

    @Test
    @Order(15)
    @DisplayName("Roll 입장 - 실패 (url로 roll 조회 x)")
    void joinRoll_fail_roll_null() {

        // GIVEN
        String url = urlGenerator.generateUrl();
        RollJoinRequestDto requestDto = new RollJoinRequestDto("테스트", 1234, 1234);

        given(rollRepository.findByUrl(url)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RollException.class, () -> rollService.joinRoll(url, requestDto));
    }

    @Test
    @Order(16)
    @DisplayName("Roll 입장 - 실패 (학급 코드 불일치)")
    void joinRoll_fail_classCode_not_Valid() {

        // GIVEN
        String url = urlGenerator.generateUrl();
        RollJoinRequestDto requestDto = new RollJoinRequestDto("테스트", 1234, 1234);

        Roll roll = Roll.builder()
                .id(1L)
                .classCode(5678)
                .url(url)
                .build();

        given(rollRepository.findByUrl(url)).willReturn(Optional.of(roll));

        // WHEN & THEN
        assertThrows(RollException.class, () -> rollService.joinRoll(url, requestDto));
    }

    @Test
    @Order(17)
    @DisplayName("Roll 입장 - 실패 (학생 정보 X -> 학생 저장")
    void joinRoll_fail_student_save() {

        // GIVEN
        String url = urlGenerator.generateUrl();
        RollJoinRequestDto requestDto = new RollJoinRequestDto("테스트", 1234, 1234);

        Roll roll = Roll.builder()
                .id(1L)
                .rollName("테스트용")
                .classCode(1234)
                .url(url)
                .build();

        Student student = Student.builder()
                .id(1L)
                .pinNumber(1234)
                .name("테스트")
                .build();

        List<PaperResponseDTO> papers = List.of(new PaperResponseDTO());

        given(rollRepository.findByUrl(url)).willReturn(Optional.of(roll));
        given(studentRepository.findByNameAndPinNumber("홍길동", 1010)).willReturn(Optional.empty());
        given(studentRepository.save(any(Student.class))).willReturn(student);
        given(paperService.getPapers(roll.getId())).willReturn(papers);

        //WHEN
        RollJoinResponseDto responseDto = rollService.joinRoll(url, requestDto);

        //THEN
        assertThat(responseDto.getRollId()).isEqualTo(roll.getId());
        assertThat(responseDto.getRollName()).isEqualTo(roll.getRollName());
        assertThat(responseDto.getStudentName()).isEqualTo(student.getName());
        assertThat(responseDto.getPapers().size()).isEqualTo(papers.size());
    }

    @Test
    @Order(18)
    @DisplayName("Roll 입장 - 성공 (paper 빈 목록 응답")
    void joinRoll_success_non_paper() {

        //GIVEN
        String url = urlGenerator.generateUrl();
        RollJoinRequestDto requestDto = new RollJoinRequestDto("테스트", 1234, 1234);

        Student student = Student.builder()
                .id(1L)
                .pinNumber(1234)
                .name("테스트")
                .build();

        Roll roll = Roll.builder()
                .id(1L)
                .rollName("테스트용 롤")
                .classCode(1234)
                .url(url)
                .build();

        given(rollRepository.findByUrl(url)).willReturn(Optional.of(roll));
        given(studentRepository.findByNameAndPinNumber(student.getName(), 1234)).willReturn(Optional.of(student));
        given(paperService.getPapers(roll.getId())).willReturn(Collections.emptyList());

        //WHEN
        RollJoinResponseDto responseDto = rollService.joinRoll(url, requestDto);

        //THEN
        assertThat(responseDto.getRollId()).isEqualTo(roll.getId());
        assertThat(responseDto.getRollName()).isEqualTo(roll.getRollName());
        assertThat(responseDto.getStudentName()).isEqualTo(student.getName());
        assertThat(responseDto.getPapers().size()).isEqualTo(0);

    }

}

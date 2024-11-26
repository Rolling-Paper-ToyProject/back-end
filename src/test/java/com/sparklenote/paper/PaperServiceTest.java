package com.sparklenote.paper;


import com.sparklenote.common.exception.PaperException;
import com.sparklenote.common.exception.RollException;
import com.sparklenote.common.exception.UserException;
import com.sparklenote.domain.entity.Paper;
import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.repository.PaperRepository;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import com.sparklenote.student.userDetails.CustomStudentDetails;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaperServiceTest {

    @Mock
    private PaperRepository paperRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RollRepository rollRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaperService paperService;

    private Authentication authentication;
    private SecurityContext securityContext;

    private void setUpTeacherAuthentication() {
        CustomOAuth2User teacher = mock(CustomOAuth2User.class);
        when(teacher.getUsername()).thenReturn("teacher@test.com");

        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(teacher);

        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setUpStudentAuthentication() {
        CustomStudentDetails studentDetails = mock(CustomStudentDetails.class);
        when(studentDetails.getUsername()).thenReturn("student");

        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(studentDetails);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    //테스트용 공동 데이터
    private final Long TEST_ROLL_ID = 1L;
    private final Long TEST_PAPER_ID = 1L;
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USER_USERNAME = "teacher@test.com";
    private final String TEST_USER_NAME = "teacher";
    private final Long TEST_STUDENT_ID = 1L;
    private final String TEST_CONTENT = "테스트 내용";

    private PaperRequestDTO createTestPaperRequestDTO() {
        return new PaperRequestDTO(TEST_CONTENT);
    }

    @Test
    @Order(1)
    @DisplayName("선생님이 페이퍼 작성 - 성공")
    void createPaper_succes() {
        //given
        setUpTeacherAuthentication();

        User teacher = User.builder()
                .id(TEST_USER_ID)
                .username(TEST_USER_USERNAME)
                .name(TEST_USER_NAME)
                .build();

        Roll roll = Roll.builder()
                .id(TEST_ROLL_ID)
                .build();

        Paper paper = Paper.builder()
                .id(TEST_PAPER_ID)
                .content(TEST_CONTENT)
                .user(teacher)
                .roll(roll)
                .build();

        PaperRequestDTO request = createTestPaperRequestDTO();

        //mock 동작
        when(userRepository.findByUsername(TEST_USER_USERNAME))
                .thenReturn(Optional.of(teacher));

        when(rollRepository.findById(TEST_ROLL_ID))
                .thenReturn(Optional.of(roll));

        when(paperRepository.save(any(Paper.class)))
                .thenReturn(paper);


        //when
        PaperResponseDTO response = paperService.createPaper(TEST_ROLL_ID, request);

        //then
        assertThat(response.getPaperId()).isEqualTo(TEST_PAPER_ID);
        assertThat(response.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(response.getAuthorName()).isEqualTo(TEST_USER_NAME);
        assertThat(response.getAuthorRole()).isEqualTo("TEACHER");

        // 메서드 호출 검증
        verify(userRepository).findByUsername(TEST_USER_USERNAME);
        verify(rollRepository).findById(TEST_ROLL_ID);
        verify(paperRepository).save(any(Paper.class));
    }

    @Test
    @Order(2)
    @DisplayName("선생님이 페이퍼 작성 - 실패(선생님이 존재하지 않음)")
    void createPaper_fail_teacherNotFound() {
        //given
        setUpTeacherAuthentication();
        PaperRequestDTO request = createTestPaperRequestDTO();

        //when 선생님을 찾을 수 없는 상황
        when(userRepository.findByUsername(TEST_USER_USERNAME))
                .thenReturn(Optional.empty());

        //then
        assertThrows(UserException.class, () -> {
            paperService.createPaper(TEST_ROLL_ID, request);
        });

        //메서드 호출 검증
        verify(userRepository).findByUsername(TEST_USER_USERNAME);
        verify(rollRepository, never()).findById(TEST_ROLL_ID);
        verify(paperRepository, never()).save(any(Paper.class));
    }

    @Test
    @Order(3)
    @DisplayName("선생님이 페이퍼 작성 -실패(Roll이 존재하지 않음)")
    void createPaper_fail_rollNotFound() {
        //given
        setUpTeacherAuthentication();

        User teacher = User.builder()
                .id(TEST_USER_ID)
                .username(TEST_USER_USERNAME)
                .name(TEST_USER_NAME)
                .build();

        PaperRequestDTO request = createTestPaperRequestDTO();

        //teacher는 찾았고 Roll은 찾을 수 없음 ( 이런 상황이 있을까)

        when(userRepository.findByUsername(TEST_USER_USERNAME))
                .thenReturn(Optional.of(teacher));

        when(rollRepository.findById(TEST_ROLL_ID))
                .thenReturn(Optional.empty());

        assertThrows(RollException.class, () -> {
            paperService.createPaper(TEST_ROLL_ID, request);
        });

        verify(userRepository).findByUsername(TEST_USER_USERNAME);
        verify(rollRepository).findById(TEST_ROLL_ID);
        verify(paperRepository, never()).save(any(Paper.class));
    }

    @Test
    @Order(4)
    @DisplayName("선생님이 페이퍼 수정 - 성공")
    void updatePaper_teacher_success() {
        setUpTeacherAuthentication();

        User teacher = User.builder()
                .id(TEST_USER_ID)
                .username(TEST_USER_USERNAME)
                .name(TEST_USER_NAME)
                .build();

        Paper paper = Paper.builder()
                .id(TEST_PAPER_ID)
                .content("원래 내용")
                .user(teacher)
                .createdBy(Paper.CreatedBy.USER)
                .build();

        PaperRequestDTO request = createTestPaperRequestDTO(); //수정 내용

        //mock
        when(userRepository.findByUsername(TEST_USER_USERNAME))
                .thenReturn(Optional.of(teacher));
        when(paperRepository.findById(TEST_PAPER_ID))
                .thenReturn(Optional.of(paper));
        when(paperRepository.save(any(Paper.class)))
                .thenReturn(paper);

        //when
        PaperResponseDTO response = paperService.updatePaper(TEST_PAPER_ID, request);

        assertThat(response.getContent()).isEqualTo(TEST_CONTENT);

        verify(paperRepository).findById(TEST_PAPER_ID);
        verify(paperRepository).save(any(Paper.class));
    }

    @Test
    @Order(6)
    @DisplayName("선생님이 페이퍼 수정 - 실패(페이퍼가 존재하지 않음)")
    void updatePaper_fail_PaperNotFound() {
        // given
        setUpTeacherAuthentication();

        User teacher = User.builder()
                .id(TEST_USER_ID)
                .username(TEST_USER_USERNAME)
                .name(TEST_USER_NAME)
                .build();

        PaperRequestDTO request = createTestPaperRequestDTO(); // 수정하려는 내용

        // 선생님 인증 정보를 설정
        when(userRepository.findByUsername(TEST_USER_USERNAME))
                .thenReturn(Optional.of(teacher));

        // 페이퍼가 존재하지 않는 상황 설정
        when(paperRepository.findById(TEST_PAPER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(PaperException.class, () -> {
            paperService.updatePaper(TEST_PAPER_ID, request);
        });

        // verify: 선생님 정보는 정상적으로 조회되었지만, 페이퍼 저장 로직은 호출되지 않음
        verify(userRepository).findByUsername(TEST_USER_USERNAME);
        verify(paperRepository).findById(TEST_PAPER_ID);
        verify(paperRepository, never()).save(any(Paper.class));
    }

    @Test
    @Order(7)
    @DisplayName("선생님이 페이퍼 삭제 - 성공")
    void deletePaper_teacher_success() {
        setUpTeacherAuthentication();

        User teacher = User.builder()
                .id(TEST_USER_ID)
                .username(TEST_USER_USERNAME)
                .name(TEST_USER_NAME)
                .build();

        Paper paper = Paper.builder()
                .id(TEST_PAPER_ID)
                .content(TEST_CONTENT)
                .user(teacher)
                .createdBy(Paper.CreatedBy.USER)
                .build();

        when(userRepository.findByUsername(TEST_USER_USERNAME))
                .thenReturn(Optional.of(teacher));

        when(paperRepository.findById(TEST_PAPER_ID))
                .thenReturn(Optional.of(paper));

        //when
        paperService.deletePaper(TEST_PAPER_ID);

        //then
        verify(userRepository).findByUsername(TEST_USER_USERNAME);
        verify(paperRepository).findById(TEST_PAPER_ID);
        verify(paperRepository).delete(paper);
    }
}

package com.sparklenote.paper.service;

import com.sparklenote.common.exception.*;
import com.sparklenote.domain.entity.Paper;
import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.repository.PaperRepository;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import com.sparklenote.user.student.CustomStudentDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


import static com.sparklenote.common.error.code.GlobalErrorCode.INTERNAL_SERVER_ERROR;
import static com.sparklenote.common.error.code.GlobalErrorCode.UNAUTHORIZED;
import static com.sparklenote.common.error.code.PaperErrorCode.PAPER_DELETE_FORBIDDEN;
import static com.sparklenote.common.error.code.PaperErrorCode.PAPER_NOT_FOUND;
import static com.sparklenote.common.error.code.RollErrorCode.ROLL_NOT_FOUND;
import static com.sparklenote.common.error.code.StudentErrorCode.STUDENT_NOT_FOUND;
import static com.sparklenote.common.error.code.UserErrorCode.USER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;
    private final StudentRepository studentRepository;
    private final RollRepository rollRepository;

    // 클라이언트 연결을 유지하기 위한 SseEmitter List (클라이언트가 여기에 저장)
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final UserRepository userRepository;

    // 이벤트 발생 시 클라이언트에게 정보를 보내는 메소드
    private void sendPaperEvent(String eventType, Paper paper) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(new PaperResponseDTO(paper.getId(), paper.getContent(), paper.getStudent().getName())));
            } catch (Exception e) {
                deadEmitters.add(emitter);  // 연결이 끊긴 경우 제거
            }
        });
        emitters.removeAll(deadEmitters); // 끊긴 emitter 제거
    }

    /**
     * paper를 생성하는 메소드
     */
    public PaperResponseDTO createPaper(PaperRequestDTO paperRequestDTO) {
        Long studentId = getAuthenticatedStudentId();
        Roll roll = rollRepository.findById(paperRequestDTO.getRollId())
                .orElseThrow(() -> new RollException(ROLL_NOT_FOUND));
        boolean isTeacher = roll.getUser().getId().equals(studentId); // Roll 생성자가 곧 선생님
        log.info("studentId: " + studentId + " rollId: " + roll.getId() + " isTeacher: " + isTeacher);
        if (isTeacher) {
            User user = userRepository.findById(studentId)
                    .orElseThrow(() -> new UserException(USER_NOT_FOUND));
            Paper paper = Paper.userFromDtoToPaper(paperRequestDTO).toBuilder()
                    .user(user)
                    .roll(roll) // Student 객체에서 가져온 Roll 설정
                    .build();
            Paper savedPaper = paperRepository.save(paper);
            sendPaperEvent("create", savedPaper);

            // 응답 DTO 생성
            return new PaperResponseDTO(savedPaper.getId(), savedPaper.getContent(), user.getName());
        } else {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new StudentException(STUDENT_NOT_FOUND));
            Paper paper = Paper.studentFromDtoToPaper(paperRequestDTO).toBuilder()
                    .student(student)
                    .roll(roll) // Student 객체에서 가져온 Roll 설정
                    .build();
            Paper savedPaper = paperRepository.save(paper);
            sendPaperEvent("create", savedPaper);

            // 응답 DTO 생성
            return new PaperResponseDTO(savedPaper.getId(), savedPaper.getContent(), student.getName());
        }

    }

    /**
     * paper를 삭제하는 메소드
     */
    public void deletePaper(Long id) {
        Long studentId = getAuthenticatedStudentId();

        Paper paper = paperRepository.findById(id).orElseThrow(() -> new RuntimeException("Paper not found with id " + id));

        // 소유자 확인
        if (!paper.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        sendPaperEvent("delete", paper);
        paperRepository.delete(paper);
    }

    /**
     * paper를 수정하는 메소드
     */
    public PaperResponseDTO updatePaper(Long id, PaperRequestDTO paperRequestDTO) {
        Long studentId = getAuthenticatedStudentId();

        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new PaperException(PAPER_NOT_FOUND));

        if (!paper.getStudent().getId().equals(studentId)) {
            throw new PaperException(PAPER_DELETE_FORBIDDEN);
        }

        paper.updateContent(paperRequestDTO); // DTO를 사용해 내용 업데이트
        Paper updatedPaper = paperRepository.save(paper);
        sendPaperEvent("update", updatedPaper);

        // DTO로 변환하여 반환
        return new PaperResponseDTO(updatedPaper.getId(), updatedPaper.getContent(), updatedPaper.getStudent().getName());
    }

    /**
     * paper를 조회하는 메소드 (사용자가 처음 페이지에 입장할 때 호출되어야 함)
     */
    public List<PaperResponseDTO> getPapers(Long rollId) {
        List<Paper> papers = paperRepository.findByRoll_Id(rollId);

        return papers.stream()
                .map(paper -> new PaperResponseDTO(paper.getId(), paper.getContent(), paper.getStudent().getName()))
                .collect(Collectors.toList());
    }

    /**
     * SecurityContextHolder에서 인증된 학생 ID를 가져오는 메소드
     */
    private Long getAuthenticatedStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new GlobalException(UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomStudentDetails) {
            // 학생 로그인의 경우
            CustomStudentDetails studentDetails = (CustomStudentDetails) principal;
            return studentDetails.getStudentId();
        } else if (principal instanceof CustomOAuth2User) {
            // 선생 로그인의 경우
            CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
            return oAuth2User.getUserId();
        }
        throw new GlobalException(INTERNAL_SERVER_ERROR);
    }
}

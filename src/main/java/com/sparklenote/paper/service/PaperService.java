package com.sparklenote.paper.service;

import com.sparklenote.common.exception.PaperException;
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


import static com.sparklenote.common.error.code.PaperErrorCode.PAPER_DELETE_FORBIDDEN;
import static com.sparklenote.common.error.code.PaperErrorCode.PAPER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;
    private final StudentRepository studentRepository;
    private final RollRepository rollRepository;
    private final UserRepository userRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private void sendPaperEvent(String eventType, Paper paper) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                // 작성자 이름과 역할을 구분하여 전달
                String authorName = paper.getCreatedBy() == Paper.CreatedBy.STUDENT ?
                        paper.getStudent().getName() : paper.getUser().getName();
                String authorRole = paper.getCreatedBy() == Paper.CreatedBy.STUDENT ? "STUDENT" : "TEACHER";

                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(new PaperResponseDTO(paper.getId(), paper.getContent(), authorName, authorRole)));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }

    public PaperResponseDTO createPaper(Long rollId, PaperRequestDTO paperRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Paper savedPaper;

        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            // 선생님(User)인 경우
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            User user = userRepository.findByUsername(oAuth2User.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("선생님을 찾을 수 없습니다."));

            Roll roll = rollRepository.findById(rollId)  // paperRequestDTO.getRollId() 대신 파라미터 사용
                    .orElseThrow(() -> new IllegalArgumentException("학급을 찾을 수 없습니다."));

            savedPaper = Paper.createTeacherPaper(paperRequestDTO, user, roll);
        } else {
            // 학생인 경우
            CustomStudentDetails studentDetails = (CustomStudentDetails) authentication.getPrincipal();
            Student student = studentRepository.findById(studentDetails.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

            savedPaper = Paper.createStudentPaper(paperRequestDTO, student, student.getRoll());
        }

        savedPaper = paperRepository.save(savedPaper);
        sendPaperEvent("create", savedPaper);

        return new PaperResponseDTO(
                savedPaper.getId(),
                savedPaper.getContent(),
                savedPaper.getCreatedBy() == Paper.CreatedBy.STUDENT ?
                        savedPaper.getStudent().getName() :
                        savedPaper.getUser().getName(),
                savedPaper.getCreatedBy() == Paper.CreatedBy.STUDENT ? "STUDENT" : "TEACHER"
        );
    }

    public void deletePaper(Long id) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found with id " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            // 선생님은 모든 paper 삭제 가능
            sendPaperEvent("delete", paper);
            paperRepository.delete(paper);
        } else {
            // 학생은 자신의 paper만 삭제 가능
            CustomStudentDetails studentDetails = (CustomStudentDetails) authentication.getPrincipal();
            if (paper.getCreatedBy() != Paper.CreatedBy.STUDENT ||
                    !paper.getStudent().getId().equals(studentDetails.getStudentId())) {
                throw new RuntimeException("삭제 권한이 없습니다.");
            }
            sendPaperEvent("delete", paper);
            paperRepository.delete(paper);
        }
    }

    public PaperResponseDTO updatePaper(Long id, PaperRequestDTO paperRequestDTO) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new PaperException(PAPER_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            // 선생님은 모든 paper 수정 가능
            paper.updateContent(paperRequestDTO);
        } else {
            // 학생은 자신의 paper만 수정 가능
            CustomStudentDetails studentDetails = (CustomStudentDetails) authentication.getPrincipal();
            if (paper.getCreatedBy() != Paper.CreatedBy.STUDENT ||
                    !paper.getStudent().getId().equals(studentDetails.getStudentId())) {
                throw new PaperException(PAPER_DELETE_FORBIDDEN);
            }
            paper.updateContent(paperRequestDTO);
        }

        Paper updatedPaper = paperRepository.save(paper);
        sendPaperEvent("update", updatedPaper);

        return new PaperResponseDTO(
                updatedPaper.getId(),
                updatedPaper.getContent(),
                updatedPaper.getCreatedBy() == Paper.CreatedBy.STUDENT ?
                        updatedPaper.getStudent().getName() :
                        updatedPaper.getUser().getName(),
                updatedPaper.getCreatedBy() == Paper.CreatedBy.STUDENT ? "STUDENT" : "TEACHER"
        );
    }

    public List<PaperResponseDTO> getPapers(Long rollId) {
        List<Paper> papers = paperRepository.findByRoll_Id(rollId);

        return papers.stream()
                .map(paper -> new PaperResponseDTO(
                        paper.getId(),
                        paper.getContent(),
                        paper.getCreatedBy() == Paper.CreatedBy.STUDENT ?
                                paper.getStudent().getName() :
                                paper.getUser().getName(),
                        paper.getCreatedBy() == Paper.CreatedBy.STUDENT ? "STUDENT" : "TEACHER"
                ))
                .collect(Collectors.toList());
    }
}
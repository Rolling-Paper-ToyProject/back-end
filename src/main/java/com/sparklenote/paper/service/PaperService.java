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
                String authorName = getAuthorName(paper);
                String authorRole = getAuthorRole(paper);

                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(new PaperResponseDTO(paper.getId(), paper.getContent(), authorName, authorRole)));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }

    private String getAuthorName(Paper paper) {
        if (paper.getCreatedBy() == Paper.CreatedBy.STUDENT) {
            return paper.getStudent().getName();
        } else {
            return paper.getUser().getName();
        }
    }

    private String getAuthorRole(Paper paper) {
        if (paper.getCreatedBy() == Paper.CreatedBy.STUDENT) {
            return "STUDENT";
        } else {
            return "TEACHER";
        }
    }

    public PaperResponseDTO createPaper(Long rollId, PaperRequestDTO paperRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Paper savedPaper;

        if (authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            // 선생님(User)인 경우
            User user = userRepository.findByUsername(oAuth2User.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("선생님을 찾을 수 없습니다."));

            Roll roll = rollRepository.findById(rollId)
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

        String authorName = getAuthorName(savedPaper);
        String authorRole = getAuthorRole(savedPaper);

        return new PaperResponseDTO(
                savedPaper.getId(),
                savedPaper.getContent(),
                authorName,
                authorRole
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

        String authorName = getAuthorName(updatedPaper);
        String authorRole = getAuthorRole(updatedPaper);

        return new PaperResponseDTO(
                updatedPaper.getId(),
                updatedPaper.getContent(),
                authorName,
                authorRole
        );
    }

    public List<PaperResponseDTO> getPapers(Long rollId) {
        List<Paper> papers = paperRepository.findByRoll_Id(rollId);

        return papers.stream()
                .map(paper -> {
                    String authorName = getAuthorName(paper);
                    String authorRole = getAuthorRole(paper);
                    return new PaperResponseDTO(
                            paper.getId(),
                            paper.getContent(),
                            authorName,
                            authorRole
                    );
                })
                .collect(Collectors.toList());
    }
}
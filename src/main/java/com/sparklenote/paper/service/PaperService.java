package com.sparklenote.paper.service;

import com.sparklenote.domain.entity.Paper;
import com.sparklenote.domain.repository.PaperRepository;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;

    // 클라이언트 연결을 유지하기 위한 SseEmitter List (클라이언트가 여기에 저장)
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // 이벤트 발생 시 클라이언트에게 정보를 보내는 메소드

    /**
     * 이벤트 발생시 (생성, 수정, 삭제) emitter를 구독한 클라이언트에게 정보 송신
     */
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
    public PaperResponseDTO createPaper(PaperRequestDTO paperRequestDTO, HttpSession session) {
        String studentName = (String) session.getAttribute("studentName");
        Paper paper = Paper.fromDtoToPaper(paperRequestDTO);
        Paper savedPaper = paperRepository.save(paper);

        sendPaperEvent("create", savedPaper);
        // 응답 DTO 생성
        return new PaperResponseDTO(savedPaper.getId(), savedPaper.getContent(), studentName);
    }

    /**
     * paper를 삭제하는 메소드
     */
    public void deletePaper(Long id, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        Paper paper = paperRepository.findById(id).orElse(null);
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
    public PaperResponseDTO updatePaper(Long id, PaperRequestDTO paperRequestDTO, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found with id " + id));

        if (!paper.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        Paper updatedPaper = Paper.fromDtoToPaper(paperRequestDTO);
        paperRepository.save(updatedPaper);
        sendPaperEvent("update", updatedPaper);

        // DTO로 변환하여 반환
        return new PaperResponseDTO(updatedPaper.getId(), updatedPaper.getContent(), updatedPaper.getStudent().getName());
    }

    /**
     * poper를 조회하는 메소드 (사용자가 처음 페이지에 입장할 때 호출되어야 함)
     */
    public List<PaperResponseDTO> getPapers(Long rollId) {
        List<Paper> papers = paperRepository.findByRoll_Id(rollId);

        return papers.stream()
                .map(paper -> new PaperResponseDTO(paper.getId(), paper.getContent(), paper.getStudent().getName()))
                .collect(Collectors.toList());

    }
}

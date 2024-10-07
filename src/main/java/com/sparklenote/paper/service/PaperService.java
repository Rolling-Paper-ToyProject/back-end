package com.sparklenote.paper.service;

import com.sparklenote.domain.entity.Paper;
import com.sparklenote.domain.repository.PaperRepository;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;

    public PaperResponseDTO createPaper(PaperRequestDTO paperRequestDTO, HttpSession session) {
        String studentName = (String) session.getAttribute("studentName");
        Paper paper = Paper.fromDtoToPaper(paperRequestDTO);
        Paper savedPaper = paperRepository.save(paper);

        // 응답 DTO 생성
        return new PaperResponseDTO(savedPaper.getPaperId(), savedPaper.getContent(), studentName);
    }

    public void deletePaper(Long paperId, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        Paper paper = paperRepository.findById(paperId).orElse(null);
        // 소유자 확인
        if (!paper.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        paperRepository.delete(paper);
    }
}

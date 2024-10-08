package com.sparklenote.paper.service;

import com.sparklenote.domain.entity.Paper;
import com.sparklenote.domain.repository.PaperRepository;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public void deletePaper(Long id, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        Paper paper = paperRepository.findById(id).orElse(null);
        // 소유자 확인
        if (!paper.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        paperRepository.delete(paper);
    }

    public PaperResponseDTO updatePaper(Long id, PaperRequestDTO paperRequestDTO, HttpSession session) {
        Long studentId = (Long) session.getAttribute("studentId");
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found with id " + id));

        if (!paper.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        Paper updatedPaper = Paper.fromDtoToPaper(paperRequestDTO);
        paperRepository.save(updatedPaper);

        // DTO로 변환하여 반환
        return new PaperResponseDTO(updatedPaper.getPaperId(), updatedPaper.getContent(), updatedPaper.getStudent().getName());
    }

    public List<PaperResponseDTO> getPapers(Long rollId) {
        List<Paper> papers = paperRepository.findByRoll_Id(rollId);

        return papers.stream()
                .map(paper -> new PaperResponseDTO(paper.getPaperId(), paper.getContent(), paper.getStudent().getName()))
                .collect(Collectors.toList());

    }
}

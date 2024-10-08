package com.sparklenote.paper.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sparklenote.common.code.GlobalSuccessCode.CREATE;
import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/paper")
public class PaperController {

    private final PaperService paperService;

    @PostMapping("/create")
    public ResponseEntity<SnResponse<PaperResponseDTO>> createPaper(@Valid @RequestBody PaperRequestDTO paperRequestDTO, HttpSession session) {
        PaperResponseDTO responseDTO = paperService.createPaper(paperRequestDTO, session);
        return ResponseEntity.status(CREATE.getStatus())
                .body(new SnResponse<>(CREATE, responseDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<SnResponse<Void>> deletePaper(@PathVariable Long id, HttpSession session) {
        paperService.deletePaper(id, session);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SnResponse<PaperResponseDTO>> updatePaper(
            @PathVariable Long id,
            @RequestBody PaperRequestDTO paperRequestDTO,
            HttpSession session) {
        PaperResponseDTO responseDTO = paperService.updatePaper(id, paperRequestDTO, session);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, responseDTO));
    }

    @GetMapping("/{rollId}")
    public ResponseEntity<SnResponse<List<PaperResponseDTO>>> getPapersByRollId(@PathVariable Long rollId) {
        List<PaperResponseDTO> papers = paperService.getPapers(rollId);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, papers));
    }

}

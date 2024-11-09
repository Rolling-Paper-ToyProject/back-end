package com.sparklenote.paper.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sparklenote.common.code.GlobalSuccessCode.CREATE;
import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;

@Tag(name = "2. Paper Controller", description = "페이퍼 CRUD API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/paper")
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "Create paper", description = "paper 생성")
    @PostMapping("/rolls/{rollId}")
    public ResponseEntity<SnResponse<PaperResponseDTO>> createPaper(
            @PathVariable Long rollId,
            @Valid @RequestBody PaperRequestDTO paperRequestDTO) {
        PaperResponseDTO responseDTO = paperService.createPaper(rollId,paperRequestDTO);
        return ResponseEntity.status(CREATE.getStatus())
                .body(new SnResponse<>(CREATE, responseDTO));
    }

    @Operation(summary = "Delete paper", description = "paper 삭제")
    @DeleteMapping("/{id}")    // DELETE /paper/{id}
    public ResponseEntity<SnResponse<Void>> deletePaper(@PathVariable Long id) {
        paperService.deletePaper(id);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, null));
    }

    @Operation(summary = "Update paper", description = "paper 수정")

    @PutMapping("/{id}")    // PUT /paper/{id}
    public ResponseEntity<SnResponse<PaperResponseDTO>> updatePaper(
            @PathVariable Long id,
            @RequestBody PaperRequestDTO paperRequestDTO) {
        PaperResponseDTO responseDTO = paperService.updatePaper(id, paperRequestDTO);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, responseDTO));
    }

    @Operation(summary = "Get papers by roll", description = "roll에 속한 paper 조회")
    @GetMapping("/rolls/{rollId}")    // GET /paper/rolls/{rollId}
    public ResponseEntity<SnResponse<List<PaperResponseDTO>>> getPapersByRollId(
            @PathVariable Long rollId) {
        List<PaperResponseDTO> papers = paperService.getPapers(rollId);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, papers));
    }
}
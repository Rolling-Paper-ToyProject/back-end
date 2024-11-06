package com.sparklenote.paper.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.flogger.Flogger;
import lombok.extern.java.Log;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sparklenote.common.code.GlobalSuccessCode.CREATE;
import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;


@RestController
@RequiredArgsConstructor
@RequestMapping("/paper")
@Tag(name = "Paper Controller", description = "페이퍼 CRUD (생성, 조회, 수정, 삭제)")
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "/paper/create", description = "paper 생성")
    @PostMapping("/create")
    public ResponseEntity<SnResponse<PaperResponseDTO>> createPaper(@Valid @RequestBody PaperRequestDTO paperRequestDTO) {
        PaperResponseDTO responseDTO = paperService.createPaper(paperRequestDTO);
        return ResponseEntity.status(CREATE.getStatus())
                .body(new SnResponse<>(CREATE, responseDTO));
    }

    @Operation(summary = "/paper/delete/{id}", description = "paper 삭제")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<SnResponse<Void>> deletePaper(@PathVariable("id") Long id) {
        paperService.deletePaper(id);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, null));
    }

    @Operation(summary = "/paper/update/{id}", description = "paper 수정")
    @PutMapping("/update/{id}")
    public ResponseEntity<SnResponse<PaperResponseDTO>> updatePaper(
            @PathVariable("id") Long id,
            @RequestBody PaperRequestDTO paperRequestDTO) {
        PaperResponseDTO responseDTO = paperService.updatePaper(id, paperRequestDTO);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, responseDTO));
    }

    @Operation(summary = "/paper/{rollId}", description = "paper 조회")
    @GetMapping("/{rollId}")
    public ResponseEntity<SnResponse<List<PaperResponseDTO>>> getPapersByRollId(@PathVariable("rollId") Long rollId) {
        List<PaperResponseDTO> papers = paperService.getPapers(rollId);
        return ResponseEntity.status(SUCCESS.getStatus())
                .body(new SnResponse<>(SUCCESS, papers));
    }
}

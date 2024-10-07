package com.sparklenote.paper.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.paper.dto.request.PaperRequestDTO;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sparklenote.common.code.GlobalSuccessCode.CREATE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/paper")
public class PaperController {

    private final PaperService paperService;

    @PostMapping("/create")
    public ResponseEntity<SnResponse<PaperResponseDTO>> paperCreate(@Valid @RequestBody PaperRequestDTO paperRequestDTO, HttpSession session) {
        PaperResponseDTO responseDTO = paperService.createPaper(paperRequestDTO, session);
        return ResponseEntity.status(CREATE.getStatus())
                .body(new SnResponse<>(CREATE, responseDTO));
    }
}

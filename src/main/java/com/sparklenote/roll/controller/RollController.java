package com.sparklenote.roll.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.service.RollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.sparklenote.common.code.GlobalSuccessCode.CREATE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roll")
@Tag(name = "Roll 생성", description = "roll 등록 API")
public class RollController {
    private final RollService rollService;

    /**
     * 학급 게시판(Roll) 생성
     */
    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "학급 roll 생성",description = "학급 roll 생성")
    public ResponseEntity<SnResponse<?>> createRoll(@Valid @RequestBody RollCreateRequestDto createRequestDto) {
        return ResponseEntity.status(CREATE.getStatus())
                .body(new SnResponse<>(CREATE, Map.of("id", rollService.createRoll(createRequestDto).getId())));
    }
}
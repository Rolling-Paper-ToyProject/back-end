package com.sparklenote.roll.controller;

import com.sparklenote.common.response.SnResponse;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.request.RollUpdateRequestDto;
import com.sparklenote.roll.dto.response.RollJoinResponseDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.service.RollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import static com.sparklenote.common.code.GlobalSuccessCode.CREATE;
import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roll")
@Tag(name = "Roll 생성", description = "roll 등록 API")
public class RollController {
    private final RollService rollService;

    /**
     * 학급 게시판(Roll) 생성
     */
    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "학급 roll 생성", description = "학급 roll 생성")
    public ResponseEntity<SnResponse<RollResponseDTO>> createRoll(@Valid @RequestBody RollCreateRequestDto createRequestDto) {
        RollResponseDTO responseDto = rollService.createRoll(createRequestDto);
        return ResponseEntity.status(CREATE.getStatus()) // 201 Created 상태 코드
                .body(new SnResponse<>(CREATE, responseDto));
    }
    @GetMapping(value = "/{id}")
    @Operation(summary = "ID로 롤 조회", description = "주어진 ID로 롤을 조회합니다.")
    public ResponseEntity<SnResponse<RollResponseDTO>> getRollById(@PathVariable Long id) {
        RollResponseDTO rollResponseDTO = rollService.getRollById(id);
        return ResponseEntity.ok(new SnResponse<>(SUCCESS, rollResponseDTO));
    }
    @PostMapping(value = "/join/{url}")
    @Operation(summary = "학생이 Roll에 입장", description = "주어진 URL과 학급 코드를 사용하여 Roll에 입장합니다.")
    public ResponseEntity<SnResponse<RollJoinResponseDto>> joinRoll(@PathVariable String url, @RequestBody RollJoinRequestDto joinRequestDto) {
        RollJoinResponseDto responseDto = rollService.joinRoll(url, joinRequestDto);
        return ResponseEntity.ok(new SnResponse<>(SUCCESS, responseDto));
    }

    /**
     * 학급 게시판(Roll) 이름 수정
     */
    @PutMapping(value = "/update/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "학급 roll 수정", description = "주어진 ID로 학급 roll 이름을 수정합니다.")
    public ResponseEntity<SnResponse<RollResponseDTO>> updateRollName(@PathVariable Long id, @Valid @RequestBody RollUpdateRequestDto updateRequestDto) {
        RollResponseDTO responseDto = rollService.updateRollName(id, updateRequestDto);
        return ResponseEntity.ok(new SnResponse<>(SUCCESS, responseDto));
    }
    /**
     * 학급 게시판(Roll) 삭제
     */
    @DeleteMapping(value = "/delete/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "학급 roll 삭제", description = "주어진 ID를 사용하여 학급 roll을 삭제합니다.")
    public ResponseEntity<SnResponse<Void>> deleteRoll(@PathVariable Long id) {
        rollService.deleteRoll(id);
        return ResponseEntity.status(SUCCESS.getStatus()) // 204 No Content 상태 코드
                .body(new SnResponse<>(SUCCESS, null));
    }
}


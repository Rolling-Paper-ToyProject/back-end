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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sparklenote.common.code.GlobalSuccessCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roll")
@Tag(name = "Roll Controller", description = "roll 등록 API")
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

    /**
     * 선생님이 마이페이지에서 만든 학급 게시판(Roll) 조회
     */
    @GetMapping("/my/rolls")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "내 Roll 목록 조회", description = "현재 로그인한 사용자의 모든 Roll 목록을 조회합니다.")
    public ResponseEntity<SnResponse<List<RollResponseDTO>>> getMyRolls() {
        List<RollResponseDTO> rollList = rollService.getMyRolls();
        return ResponseEntity.ok(new SnResponse<>(SUCCESS, rollList));
    }

    /**
     * 학급 게시판(Roll) 이름 수정
     */
    @PutMapping(value = "/update/{rollId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "학급 roll 수정", description = "주어진 ID로 학급 roll 이름을 수정합니다.")
    public ResponseEntity<SnResponse<RollResponseDTO>> updateRollName(@PathVariable("rollId") Long rollId, @Valid @RequestBody RollUpdateRequestDto updateRequestDto) {
        RollResponseDTO responseDto = rollService.updateRollName(rollId, updateRequestDto);
        return ResponseEntity.ok(new SnResponse<>(SUCCESS, responseDto));
    }

    /**
     * 학급 게시판(Roll) 삭제
     */
    @DeleteMapping(value = "/delete/{rollId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "학급 roll 삭제", description = "주어진 ID를 사용하여 학급 roll을 삭제합니다.")
    public ResponseEntity<SnResponse<Void>> deleteRoll(@PathVariable("rollId") Long rollId) {
        rollService.deleteRoll(rollId);
        return ResponseEntity.status(NO_CONTENT.getStatus()) // 204 No Content 상태 코드
                .body(new SnResponse<>(NO_CONTENT, null));
    }

    /**
     * 학급 게시판(Roll) 입장
     */
    @PostMapping(value = "/join/{url}")
    @Operation(summary = "학생이 Roll에 입장", description = "주어진 URL과 학급 코드를 사용하여 Roll에 입장합니다.")
    public ResponseEntity<SnResponse<RollJoinResponseDto>> joinRoll(@PathVariable("url") String url, @Valid @RequestBody RollJoinRequestDto joinRequestDto, HttpServletResponse response) {
        RollJoinResponseDto responseDto = rollService.joinRoll(url, joinRequestDto, response);
        return ResponseEntity.ok(new SnResponse<>(SUCCESS, responseDto));
    }
}


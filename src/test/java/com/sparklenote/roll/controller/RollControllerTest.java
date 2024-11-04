package com.sparklenote.roll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparklenote.common.exception.RollException;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.request.RollUpdateRequestDto;
import com.sparklenote.roll.dto.response.RollJoinResponseDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.service.RollService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.sparklenote.common.error.code.RollErrorCode.ROLL_NAME_NOT_CHANGED;
import static com.sparklenote.common.error.code.RollErrorCode.ROLL_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RollController.class) // 컨트롤러만 로드하여 테스트
class RollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RollService rollService; // 서비스 계층을 Mock으로 설정

    @Test
    @DisplayName("Roll 생성 - 성공")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
        // 인증된 사용자 설정
    void createRoll_success() throws Exception {

        // GIVEN : ResponseDTO와 RequestDTO 설정
        RollResponseDTO rollResponseDTO = RollResponseDTO.builder()
                .rollName("testRoll")
                .classCode(123456)
                .url("h1y2h3j2")
                .userId(1L)
                .build();
        given(rollService.createRoll(any(RollCreateRequestDto.class))).willReturn(rollResponseDTO);

        RollCreateRequestDto requestDto = new RollCreateRequestDto("testRoll");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // WHEN
        ResultActions result = mockMvc.perform(post("/roll/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()));

        // THEN
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.rollName").value("testRoll"))
                .andExpect(jsonPath("$.data.classCode").value(123456))
                .andExpect(jsonPath("$.data.url").value("h1y2h3j2"))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    @DisplayName("Roll 생성 - 실패 (RollName 누락)")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void createRoll_fail_rollNameNull() throws Exception {
        // GIVEN : RollName 누락 (필수 필드)
        RollCreateRequestDto requestDto = new RollCreateRequestDto("");
        String requestBody = objectMapper.writeValueAsString(requestDto); // DTO를 JSON 형식으로 변환

        // WHEN : API 호출
        ResultActions result = mockMvc.perform(post("/roll/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())
        );

        // THEN
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Roll 조회 - 성공")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void getRoll_success() throws Exception {

        // GIVEN : Roll 2개 생성
        RollResponseDTO roll1 = RollResponseDTO.builder()
                .rollName("testRoll1")
                .classCode(123456)
                .url("h1y2h3j2")
                .userId(1L)
                .build();

        RollResponseDTO roll2 = RollResponseDTO.builder()
                .rollName("testRoll2")
                .classCode(789123)
                .url("abc123")
                .userId(1L)
                .build();

        List<RollResponseDTO> rollList = Arrays.asList(roll1, roll2);
        given(rollService.getMyRolls()).willReturn(rollList);

        // When : API 호출
        ResultActions result = mockMvc.perform(get("/roll/my/rolls")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        // THEN
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].rollName").value("testRoll1"))
                .andExpect(jsonPath("$.data[0].classCode").value(123456))
                .andExpect(jsonPath("$.data[0].url").value("h1y2h3j2"))
                .andExpect(jsonPath("$.data[0].userId").value(1L))
                .andExpect(jsonPath("$.data[1].rollName").value("testRoll2"))
                .andExpect(jsonPath("$.data[1].classCode").value(789123))
                .andExpect(jsonPath("$.data[1].url").value("abc123"))
                .andExpect(jsonPath("$.data[1].userId").value(1L));
    }

    @Test
    @DisplayName("Roll 조회 - 성공 (만든 Roll이 없을 때, 비어있는 상태를 반환)")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void getRoll_failed() throws Exception {

        // GIVEN : 생성된 Roll 없음
        given(rollService.getMyRolls()).willReturn(Collections.emptyList());

        // When : API 호출
        ResultActions result = mockMvc.perform(get("/roll/my/rolls")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        // THEN
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Roll 수정 - 성공")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void updateRoll_success() throws Exception {

        // GIVEN
        RollUpdateRequestDto requestDto = new RollUpdateRequestDto("updatedRollName");
        Long rollId = 123L;
        RollResponseDTO responseDTO = RollResponseDTO.builder()
                .rollName("updatedRollName")
                .classCode(123456)
                .url("abc123")
                .userId(1L)
                .build();

        given(rollService.updateRollName(eq(rollId), any(RollUpdateRequestDto.class))).willReturn(responseDTO);

        // WHEN : API 호출
        ResultActions result = mockMvc.perform(put("/roll/update/" + rollId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf())
        );

        // THEN
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rollName").value("updatedRollName"))
                .andExpect(jsonPath("$.data.classCode").value(123456))
                .andExpect(jsonPath("$.data.url").value("abc123"))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    @DisplayName("Roll 수정 - 실패 (존재하지 않는 ID)")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void updateRoll_fail_notFound() throws Exception {

        // GIVEN
        Long rollId = 999L; // 존재하지 않는 ID
        RollUpdateRequestDto requestDto = new RollUpdateRequestDto("updatedRollName");

        given(rollService.updateRollName(eq(rollId), any(RollUpdateRequestDto.class)))
                .willThrow(new RollException(ROLL_NOT_FOUND));

        // WHEN : API 호출
        ResultActions result = mockMvc.perform(put("/roll/update/" + rollId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()));

        // THEN
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Roll 삭제 - 성공")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void deleteRoll_success() throws Exception {

        // GIVEN
        Long rollId = 123L;
        doNothing().when(rollService).deleteRoll(eq(rollId));

        // WHEN
        ResultActions result = mockMvc.perform(delete("/roll/delete/" + rollId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        // THEN
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Roll 삭제 - 실패 (존재하지 않는 ID)")
    @WithMockUser(username = "testUsername", roles = "TEACHER")
    void deleteRoll_fail_notFound() throws Exception {
        Long rollId = 999L; // 존재하지 않는 ID

        // 서비스에서 예외 발생하도록 설정
        doThrow(new RollException(ROLL_NOT_FOUND)).when(rollService).deleteRoll(eq(rollId));

        // WHEN : API 호출
        ResultActions result = mockMvc.perform(delete("/roll/delete/" + rollId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        // THEN : 404 Not Found 상태 코드와 예외 메시지 확인
        result.andExpect(status().isNotFound());
    }

//    @Test
//    @DisplayName("Roll 입장 - 성공 (Student가 Roll에 입장")
//    void joinRoll_success() throws Exception {
//
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken("studentUsername", null, Collections.singletonList(new SimpleGrantedAuthority(Role.STUDENT.name())))
//        );
//
//        // GIVEN
//        String url = "abc123";
//        RollJoinRequestDto requestDto = new RollJoinRequestDto("testUser", 1234);
//        RollJoinResponseDto responseDto = RollJoinResponseDto.builder()
//                .name("testUser")
//                .studentId(123L)
//                .build();
//
//       // given(rollService.joinRoll(eq(url), any(RollJoinRequestDto.class))).willReturn(responseDto);
//
//        // WHEN : API 호출
//        ResultActions result = mockMvc.perform(post("/roll/join/" + url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestDto))
//                .with(csrf())
//        );
//
//        // THEN
//        result.andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.data.name").value("testUser"))
//                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
//                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
//
//    }

    @Test
    @DisplayName("Roll 참여 - 실패 (유효하지 않은 PIN 번호)")
    void joinRoll_fail_invalidPin() throws Exception {
        // GIVEN : 유효하지 않은 PIN 번호 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("studentUsername", null, Collections.singletonList(new SimpleGrantedAuthority(Role.STUDENT.name())))
        );

        String url = "abc123";
        RollJoinRequestDto requestDto = new RollJoinRequestDto("testStudent", 123); // PIN이 4자리가 아님

        // WHEN : API 호출
        ResultActions result = mockMvc.perform(post("/roll/join/" + url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()));

        // THEN : 400 상태 코드와 유효성 검사 오류 메시지 검증
        result.andExpect(status().isBadRequest());
    }

}

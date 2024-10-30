package com.sparklenote.roll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.service.RollService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("Roll 생성 - MockMvc 방식")
    @WithMockUser(username = "testUsername", roles = "TEACHER") // 인증된 사용자 설정
    void createRoll_shouldReturnCreated() throws Exception {

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
}

package com.sparklenote.roll.dto.response;

import com.sparklenote.paper.dto.response.PaperResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollJoinResponseDto {

    private Long rollId;
    private String rollName;
    private String studentName;
    private List<PaperResponseDTO> papers;
    private String accessToken;
    private String refreshToken;
}
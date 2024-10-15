package com.sparklenote.roll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollJoinResponseDto {
    private String url;
    private int classCode;
    private String rollName;
    private String studentName;
    private String message;
}
package com.sparklenote.roll.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollJoinRequestDto {

    private String url;         // 롤의 URL
    private int classCode;   // 학생이 입력한 학급 코드
    private String studentName;
}

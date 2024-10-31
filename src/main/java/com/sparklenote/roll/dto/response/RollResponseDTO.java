package com.sparklenote.roll.dto.response;


import com.sparklenote.domain.entity.Roll;
import lombok.*;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollResponseDTO {
    private String rollName;  // 사용자가 입력한 학급 이름
    private int classCode;    // 서버가 생성한 학급 코드
    private String url;       // 서버가 생성한 URL
    private Long userId;      // 사용자 ID
    private Long rollId;

    // DTO 변환 메서드
    public static RollResponseDTO fromRoll(Roll roll, Long userId) {
        return RollResponseDTO.builder()
                .rollName(roll.getRollName())
                .classCode(roll.getClassCode()) // 변경된 변수명 사용
                .url(roll.getUrl())
                .userId(userId)
                .rollId(roll.getId())
                .build();
    }
}

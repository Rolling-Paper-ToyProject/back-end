package com.sparklenote.roll.dto.response;


import lombok.*;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollResponseDTO {
    private String rollName;
    private String rollCode;
    private String url;
    private Long userId;
}
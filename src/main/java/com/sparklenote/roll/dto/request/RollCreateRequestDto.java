package com.sparklenote.roll.dto.request;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollCreateRequestDto {
    private String rollName;
}

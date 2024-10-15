package com.sparklenote.roll.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RollUpdateRequestDto {

    @NotBlank(message = "학급 이름은 필수입니다")
    private String rollName;
}

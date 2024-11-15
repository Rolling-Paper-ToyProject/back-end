package com.sparklenote.roll.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollUpdateRequestDto {

    @Size(min = 2, max = 20, message = "Roll 이름은 최소 2자 이상, 20자 미만이어야 합니다.")
    private String rollName;
}

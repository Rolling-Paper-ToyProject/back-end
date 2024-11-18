package com.sparklenote.roll.dto.request;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.enumType.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollJoinRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]*$", message = "이름은 한글과 영문만 허용됩니다.")
    @Size(min = 1, max = 5, message = "이름은 1자 이상 5자 이하여야 합니다.")
    private String name;

    @NotNull(message = "클래스 코드는 필수입니다.")
    private int classCode;

    @Min(value = 1000, message = "PIN 번호는 4자리 숫자여야 합니다.")
    @Max(value = 9999, message = "PIN 번호는 4자리 숫자여야 합니다.")
    private int pinNumber;

    public Student toStudent(Roll roll) {
        return Student.builder()
                .name(name)
                .pinNumber(pinNumber)
                .roll(roll) // Roll 객체 설정 (롤링페이퍼)
                .role(Role.STUDENT) // 역할 설정 (학생 역할)
                .build();
    }
}

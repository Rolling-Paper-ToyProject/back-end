package com.sparklenote.student.dto.response;

import com.sparklenote.domain.enumType.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDTO {
    private Long studentId;
    private String name;
    private String password;
    private Role role;

}

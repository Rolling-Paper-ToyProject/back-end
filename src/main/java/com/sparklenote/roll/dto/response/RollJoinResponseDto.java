package com.sparklenote.roll.dto.response;

import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
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

    private String rollName;
    private String name;
    List<PaperResponseDTO> papers;

}
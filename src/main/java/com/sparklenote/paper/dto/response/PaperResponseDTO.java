package com.sparklenote.paper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaperResponseDTO {

    private Long paperId;
    private String content;
    private String studentName;
}

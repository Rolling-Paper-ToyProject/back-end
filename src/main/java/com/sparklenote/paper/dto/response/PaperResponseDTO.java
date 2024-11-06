package com.sparklenote.paper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperResponseDTO {
    private Long paperId;
    private String content;
    private String authorName;
    private String authorRole;
}

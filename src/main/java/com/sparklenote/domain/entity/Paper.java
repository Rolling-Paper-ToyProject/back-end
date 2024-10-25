package com.sparklenote.domain.entity;

import com.sparklenote.paper.dto.request.PaperRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Paper extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    private Long id;

    private String content;

    private String sticker;

    @ManyToOne
    @JoinColumn(name = "roll_id")
    private Roll roll;

    @OneToMany(mappedBy = "paper")
    private List<Sticker> stickers = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student; // 학생에 대한 외래 키 (User)

    public static Paper fromDtoToPaper(PaperRequestDTO paperRequestDTO) {
        Paper paper = Paper.builder()
                .content(paperRequestDTO.getContent())
                .build();
        return paper;
    }

    // 수정 작업을 위한 메서드 (필드 업데이트용)
    public void updateContent(PaperRequestDTO paperRequestDTO) {
        this.content = paperRequestDTO.getContent();
    }

}

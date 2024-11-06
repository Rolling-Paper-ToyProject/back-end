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
public class Paper extends BaseTimeEntity {

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
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "user_id")    // User(teacher) 추가
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by")
    private CreatedBy createdBy;     // 작성자 타입 구분

    public enum CreatedBy {
        STUDENT, USER  // USER는 선생님을 의미
    }

    public static Paper fromDtoToPaper(PaperRequestDTO paperRequestDTO) {
        return Paper.builder()
                .content(paperRequestDTO.getContent())
                .build();
    }

    // 학생용 Paper 생성
    public static Paper createStudentPaper(PaperRequestDTO paperRequestDTO, Student student, Roll roll) {
        return Paper.builder()
                .content(paperRequestDTO.getContent())
                .student(student)
                .roll(roll)
                .createdBy(CreatedBy.STUDENT)
                .build();
    }

    // 선생님용 Paper 생성
    public static Paper createTeacherPaper(PaperRequestDTO paperRequestDTO, User user, Roll roll) {
        return Paper.builder()
                .content(paperRequestDTO.getContent())
                .user(user)
                .roll(roll)
                .createdBy(CreatedBy.USER)
                .build();
    }

    public void updateContent(PaperRequestDTO paperRequestDTO) {
        this.content = paperRequestDTO.getContent();
    }
}
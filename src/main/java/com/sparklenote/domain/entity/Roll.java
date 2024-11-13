package com.sparklenote.domain.entity;

import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Roll extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roll_id")
    private Long id;

    @Column(name = "roll_name")
    private String rollName;

    @Column(name = "class_code")
    private int classCode;

    private String url;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "roll", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "roll")
    private List<Paper> papers = new ArrayList<>();

    // 학급 코드와 URL이 유효한지 확인
    public boolean validateClassCode(int inputClassCode) {
        return classCode == inputClassCode;
    }

    // 팩토리 메서드: 학급 코드와 URL을 포함하여 객체를 생성
    public static Roll createRollFromDto(RollCreateRequestDto createRequestDto, int classCode, String url, User username) {
        return Roll.builder()
                .rollName(createRequestDto.getRollName())
                .classCode(classCode)  // 빌더를 통해 학급 코드 설정
                .url(url)  // 빌더를 통해 URL 설정
                .user(username)
                .build();
    }
    // 이름 수정 메서드
    public void updateName(String rollName) {
        this.rollName = rollName;
    }
}




package com.sparklenote.domain.entity;

import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.enumType.SocialType;
import com.sparklenote.user.dto.request.UserRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;
    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Roll> rolls = new ArrayList<>();

    @OneToMany(mappedBy = "student") // Paper에서 student로 매핑된 필드
    private List<Paper> papers = new ArrayList<>(); // 학생이 작성한 Paper 목록

    public void updateFromDTO(UserRequestDTO userRequestDTO) {
        if (userRequestDTO.getEmail() != null) {
            this.email = userRequestDTO.getEmail();
        }
        if (userRequestDTO.getName() != null) {
            this.name = userRequestDTO.getName();
        }
    }
    // 학생용 정보 업데이트 메서드 (이름과 역할만 변경)
    public User withUpdatedStudentInfo(String newName, Role roll) {
        return this.toBuilder()
                .name(newName)
                .role(roll)
                .build();
    }
}

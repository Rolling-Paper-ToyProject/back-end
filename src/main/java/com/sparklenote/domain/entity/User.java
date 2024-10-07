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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String username;
    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @OneToMany(mappedBy = "user")
    private List<Roll> rolls = new ArrayList<>();

    public void updateFromDTO(UserRequestDTO userRequestDTO) {
        if (userRequestDTO.getEmail() != null) {
            this.email = userRequestDTO.getEmail();
        }
        if (userRequestDTO.getName() != null) {
            this.name = userRequestDTO.getName();
        }
    }
}

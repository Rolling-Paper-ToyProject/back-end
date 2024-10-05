package com.sparklenote.user.dto.request;

import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.enumType.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    private String username;
    private String name;
    private String email;
    private Role role;
    private SocialType socialType;

    public UserRequestDTO(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // DTO에서 User 엔티티로 변환하는 메소드
    public User toEntity() {
        return User.builder()
                .username(username)
                .email(email)
                .name(name)
                .role(role)
                .socialType(socialType)
                .build();
    }
}

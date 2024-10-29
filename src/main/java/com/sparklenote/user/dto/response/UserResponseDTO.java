package com.sparklenote.user.dto.response;

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
public class UserResponseDTO {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private Role role;
    private SocialType socialType;

    public UserResponseDTO(String username, String name, String email, Role role, SocialType socialType) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.socialType = socialType;
    }
}

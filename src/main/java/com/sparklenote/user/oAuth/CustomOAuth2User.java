package com.sparklenote.user.oAuth;

import com.sparklenote.user.dto.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserResponseDTO userResponseDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                return userResponseDTO.getRole().getAuthority();
            }
        });

        return collection;
    }

    @Override
    public String getName() {

        return userResponseDTO.getName();
    }

    public String getUsername() {

        return userResponseDTO.getUsername();
    }
}
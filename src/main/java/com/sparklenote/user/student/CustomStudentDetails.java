package com.sparklenote.user.student;

import com.sparklenote.user.student.dto.StudentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomStudentDetails implements UserDetails {

    private final StudentResponseDTO studentResponseDTO;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(studentResponseDTO.getRole().getAuthority()));
        return authorities;
    }
    @Override
    public String getPassword() {
        return studentResponseDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return studentResponseDTO.getName();
    }

    public Long getStudentId() {
        return studentResponseDTO.getStudentId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
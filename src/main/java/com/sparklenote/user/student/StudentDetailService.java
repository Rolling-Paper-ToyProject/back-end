package com.sparklenote.user.student;

import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.user.student.dto.StudentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    public class StudentDetailService implements UserDetailsService {
        private final StudentRepository studentRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            Student student = studentRepository.findByName(username)  // findById가 아닌 findByName으로 수정
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            // Student 엔티티를 StudentResponseDTO로 변환
            StudentResponseDTO dto = StudentResponseDTO.builder()
                    .studentId(student.getId())
                    .name(student.getName())
                    .password(String.valueOf(student.getPinNumber()))
                    .role(student.getRole())
                    .build();

            return new CustomStudentDetails(dto);
        }
    }
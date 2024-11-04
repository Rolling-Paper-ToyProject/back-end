package com.sparklenote.roll.service;

import com.sparklenote.common.exception.RollException;
import com.sparklenote.common.exception.UserException;
import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.Student;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.enumType.Role;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.StudentRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.request.RollUpdateRequestDto;
import com.sparklenote.roll.dto.response.RollJoinResponseDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sparklenote.common.error.code.RollErrorCode.*;
import static com.sparklenote.common.error.code.UserErrorCode.USER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class RollService {

    @Value("${jwt.accessExpiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenExpiration;

    private final RollRepository rollRepository;
    private final UserRepository userRepository;
    private final UrlGenerator urlGenerator;
    private final StudentRepository studentRepository;
    private final JWTUtil jwtUtil;

    public RollResponseDTO createRoll(RollCreateRequestDto createRequestDto) {
        int classCode = ClassCodeGenerator.generateClassCode(); // 학급 코드 생성
        String url = urlGenerator.generateUrl(); // URL 생성

        // SecurityContextHolder에서 로그인된 사용자의 username 가져오기
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = customOAuth2User.getUsername();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (user.getRole() == Role.STUDENT) {
            throw new RollException(UNAUTHORIZED_STUDENT);
        }

        // Roll 엔티티 생성
        Roll roll = Roll.fromRollCreateDto(createRequestDto, classCode, url, user);

        // Roll 저장
        Roll savedRoll = rollRepository.save(roll);

        // RollResponseDTO 생성하여 반환
        return RollResponseDTO.fromRoll(savedRoll, user.getId()); // RollResponseDTO에 URL 포함
    }

    public void deleteRoll(Long id) {
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new RollException(ROLL_NOT_FOUND));
        rollRepository.delete(roll);
    }

    public RollResponseDTO updateRollName(Long id, RollUpdateRequestDto updateRequestDto) {
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new RollException(ROLL_NOT_FOUND));

        if (roll.getRollName().equals(updateRequestDto.getRollName())) {
            throw new RollException(ROLL_NAME_NOT_CHANGED);
        }

        // Roll 이름 수정
        roll.updateName(updateRequestDto.getRollName());

        // 수정된 Roll 저장
        Roll updatedRoll = rollRepository.save(roll);

        // 수정된 Roll 정보를 DTO로 변환하여 반환
        Long userId = roll.getUser().getId();
        return RollResponseDTO.fromRoll(updatedRoll,userId);
    }

    public RollJoinResponseDto joinRoll(String url, RollJoinRequestDto joinRequestDto, HttpServletResponse response) {
        // Roll 조회 및 학급 코드 검증
        Roll roll = rollRepository.findByUrl(url)
                .orElseThrow(() -> new RollException(ROLL_NOT_FOUND));

        if(!roll.validateClassCode(joinRequestDto.getClassCode())) {
            throw new RollException(INVALID_CLASS_CODE);
        }


        // 학생 조회 또는 등록
        Optional<Student> optionalStudent = studentRepository.findByNameAndPinNumber(
                joinRequestDto.getName(),
                joinRequestDto.getPinNumber()
        );

        Student student;
        if (optionalStudent.isEmpty()) {
            // 회원가입 처리
            student = studentRepository.save(joinRequestDto.toStudent(roll));
        } else {
            // 기존 학생 정보 사용
            student = optionalStudent.get();
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                student.getId().toString(),
                Role.STUDENT, // 학생의 역할을 지정
                accessTokenExpiration
        );
        String refreshToken = jwtUtil.createRefreshToken(
                student.getId().toString(),
                refreshTokenExpiration
        );

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("RefreshToken", refreshToken);

        // 응답 DTO 생성

        return RollJoinResponseDto.builder()
                .name(student.getName())
                .build();
    }

    public List<RollResponseDTO> getMyRolls() {
        // SecurityContextHolder에서 현재 로그인된 사용자 정보 가져오기
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = customOAuth2User.getUsername();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 해당 사용자의 모든 Roll 조회
        List<Roll> rolls = rollRepository.findAllByUser(user);

        // Roll 목록을 DTO로 변환하여 반환
        return rolls.stream()
                .map(roll -> RollResponseDTO.fromRoll(roll, user.getId()))
                .collect(Collectors.toList());
    }
}

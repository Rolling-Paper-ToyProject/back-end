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
import com.sparklenote.paper.dto.response.PaperResponseDTO;
import com.sparklenote.paper.service.PaperService;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.request.RollUpdateRequestDto;
import com.sparklenote.roll.dto.response.RollJoinResponseDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.jwt.JWTUtil;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    private final PaperService paperService;
    private final JWTUtil jwtUtil;

    public RollResponseDTO createRoll(RollCreateRequestDto createRequestDto) {
        int classCode = ClassCodeGenerator.generateClassCode(); // 학급 코드 생성
        String url = urlGenerator.generateUrl(); // URL 생성

        // SecurityContextHolder에서 로그인된 사용자의 username 가져오기
        String username = getCustomOAuth2User();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (user.getRole() == Role.STUDENT) {
            throw new RollException(UNAUTHORIZED_ACCESS);
        }

        // Roll 엔티티 생성
        Roll roll = Roll.createRollFromDto(createRequestDto, classCode, url, user);

        // Roll 저장
        Roll savedRoll = rollRepository.save(roll);

        // RollResponseDTO 생성하여 반환
        return RollResponseDTO.fromRoll(savedRoll, user.getId()); // RollResponseDTO에 URL 포함
    }

    public void deleteRoll(Long id) {
        // 현재 로그인한 사용자 확인
        String username = getCustomOAuth2User();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // Roll 조회
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new RollException(ROLL_NOT_FOUND));

        // Roll의 소유자와 현재 사용자가 일치하는지 확인
        if (!roll.getUser().getId().equals(user.getId())) {
            throw new RollException(UNAUTHORIZED_ACCESS);
        }

        // 검증이 완료된 후 삭제
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

    public RollJoinResponseDto joinRoll(String url, RollJoinRequestDto joinRequestDto) {
        // Roll 조회 및 학급 코드 검증
        Roll roll = rollRepository.findByUrl(url)
                .orElseThrow(() -> new RollException(ROLL_NOT_FOUND));

        if (!roll.validateClassCode(joinRequestDto.getClassCode())) {
            throw new RollException(INVALID_CLASS_CODE);
        }

        Optional<Student> optionalStudent = studentRepository.findByNameAndPinNumberAndRollId(
                joinRequestDto.getName(),
                joinRequestDto.getPinNumber(),
                roll.getId()
        );

        Student student = optionalStudent.orElseGet(() -> {
            // 없으면 새로운 학생으로 등록
            Student newStudent = joinRequestDto.toStudent(roll);
            return studentRepository.save(newStudent);
        });

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                student.getId().toString(),
                student.getName(),
                Role.STUDENT,
                accessTokenExpiration
        );

        String refreshToken = jwtUtil.createRefreshToken(
                student.getId().toString(),
                refreshTokenExpiration
        );

        // Paper 목록 조회
        List<PaperResponseDTO> papers = paperService.getPapers(roll.getId());

        // 응답 DTO 생성
        return RollJoinResponseDto.builder()
                .studentId(student.getId())
                .rollName(roll.getRollName())
                .studentName(student.getName())
                .papers(papers)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .rollId(roll.getId())
                .role(Role.STUDENT.name())
                .build();
    }

    public List<RollResponseDTO> getMyRolls() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof CustomOAuth2User oAuth2User)) {
            throw new AccessDeniedException("선생님 타입만 가능합니다");
        }

        String username = oAuth2User.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<Roll> rolls = rollRepository.findAllByUser(user);
        return rolls.stream()
                .map(roll -> RollResponseDTO.fromRoll(roll, user.getId()))
                .collect(Collectors.toList());
    }
    private static String getCustomOAuth2User() {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customOAuth2User.getUsername();
    }
}
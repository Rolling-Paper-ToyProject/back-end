package com.sparklenote.roll.service;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.User;
import com.sparklenote.domain.repository.RollRepository;
import com.sparklenote.domain.repository.UserRepository;
import com.sparklenote.roll.dto.request.RollCreateRequestDto;
import com.sparklenote.roll.dto.request.RollJoinRequestDto;
import com.sparklenote.roll.dto.response.RollResponseDTO;
import com.sparklenote.roll.util.ClassCodeGenerator;
import com.sparklenote.roll.util.UrlGenerator;
import com.sparklenote.user.oAuth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RollService {

    private final RollRepository rollRepository;
    private final UserRepository userRepository;
    private final UrlGenerator urlGenerator;

    public RollResponseDTO createRoll(RollCreateRequestDto createRequestDto) {
        int classCode = ClassCodeGenerator.generateClassCode(); // 학급 코드 생성
        String url = urlGenerator.generateUrl(); // URL 생성

        // SecurityContextHolder에서 로그인된 사용자의 username 가져오기
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Principal type: " + principal.getClass().getName());

        String username = customOAuth2User.getUsername();

        // username으로 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Roll 엔티티 생성
        Roll roll = Roll.fromRollCreateDto(createRequestDto, classCode, url, user);

        // Roll 저장
        Roll savedRoll = rollRepository.save(roll);

        // RollResponseDTO 생성하여 반환
        return RollResponseDTO.fromRoll(savedRoll, user.getId()); // RollResponseDTO에 URL 포함

    }

    public RollResponseDTO getRollByUrl(String url) {
        Roll roll = rollRepository.findByUrl(url)
                .orElseThrow(() -> new IllegalArgumentException("해당 URL의 Roll을 찾을 수 없습니다."));
        User user = roll.getUser(); // 롤의 소유자 정보 가져오기
        return RollResponseDTO.fromRoll(roll, user.getId());
    }

    public RollJoinRequestDto joinRoll(String url, RollJoinRequestDto  joinRequestDto) {
        Roll roll = rollRepository.findByUrl(url)
                .orElseThrow(() -> new IllegalArgumentException("해당 URL의 Roll을 찾을 수 없습니다."));

        // 학급 코드 검증
        if (roll.getClassCode() != joinRequestDto.getClassCode()) { // int 비교
            throw new IllegalArgumentException("학급 코드가 일치하지 않습니다.");
        }

        // 학생 정보를 롤에 추가하는 로직
        // 예를 들어, 학생의 이름을 롤에 추가하거나 별도의 엔티티에 저장할 수 있습니다.

        // JoinResponseDTO 생성하여 반환
        return new RollJoinRequestDto(roll.getUrl(), roll.getClassCode(),roll.getUser().getUsername());
    }
}